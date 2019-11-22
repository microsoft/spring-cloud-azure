/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

public class AzureCloudConfigWatch implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCloudConfigWatch.class);

    private final Map<String, String> storeEtagMap = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ApplicationEventPublisher publisher;

    private final Map<String, Boolean> firstTimeMap = new ConcurrentHashMap<>();

    private final List<ConfigStore> configStores;

    private final Map<String, List<String>> storeContextsMap;

    private static final String CONFIGURATION_SUFFIX = "_configuration";

    private static final String FEATURE_SUFFIX = "_feature";

    private static final String FEATURE_STORE_SUFFIX = ".appconfig";

    private static final String FEATURE_STORE_WATCH_KEY = FEATURE_STORE_SUFFIX + "*";

    private Duration delay;

    private ClientStore clientStore;

    private Date lastCheckedTime;

    private String eventDataInfo;

    public AzureCloudConfigWatch(AzureCloudConfigProperties properties, Map<String, List<String>> storeContextsMap,
            ClientStore clientStore) {
        this.configStores = properties.getStores();
        this.storeContextsMap = storeContextsMap;
        this.delay = properties.getWatch().getDelay();
        this.clientStore = clientStore;
        this.eventDataInfo = "";
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * Checks configurations to see if they are no longer cached. If they are no longer
     * cached they are updated.
     * 
     * @return Future with a boolean of if a RefreshEvent was published. If
     * refreshConfigurations is currently being run elsewhere this method will return
     * right away as <b>false</b>.
     */
    public Future<Boolean> refreshConfigurations() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            completableFuture.complete(refreshStores());
            return null;
        });

        return completableFuture;
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed.
     * If any store has a value that needs to be updated a refresh event is called after
     * every store is checked.
     * @return If a refresh event is called.
     */
    private boolean refreshStores() {
        boolean needsRefresh = false;
        if (running.compareAndSet(false, true)) {
            try {
                Date notCachedTime = null;

                // LastCheckedTime isn't sent until refresh is run once, this forces a
                // eTag set on startup
                if (lastCheckedTime != null) {
                    notCachedTime = DateUtils.addSeconds(lastCheckedTime, Math.toIntExact(delay.getSeconds()));
                }
                Date date = new Date();
                if (notCachedTime == null || date.after(notCachedTime)) {
                    for (ConfigStore configStore : configStores) {
                        String watchedKeyNames = watchedKeyNames(configStore, storeContextsMap);
                        needsRefresh = refresh(configStore, CONFIGURATION_SUFFIX, watchedKeyNames) ? true
                                : needsRefresh;
                        // Refresh Feature Flags
                        needsRefresh = refresh(configStore, FEATURE_SUFFIX, FEATURE_STORE_WATCH_KEY) ? true
                                : needsRefresh;
                    }
                }
                if (needsRefresh) {
                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    RefreshEventData eventData = new RefreshEventData(this.getClass().getName());
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                }
                // Resetting last Checked date to now.
                lastCheckedTime = new Date();
            } finally {
                running.set(false);
            }
        }
        return needsRefresh;
    }

    /**
     * Checks un-cached items for etag changes. If they have changed a RefreshEventData is
     * published.
     * 
     * @param store the {@code store} for which to composite watched key names
     * @param storeSuffix Suffix used to distinguish between Settings and Features
     * @param watchedKeyNames Key used to check if refresh should occur
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private boolean refresh(ConfigStore store, String storeSuffix, String watchedKeyNames) {
        String storeNameWithSuffix = store.getName() + storeSuffix;
        SettingSelector settingSelector = new SettingSelector().setKeys(watchedKeyNames).setLabels(store.getLabels());

        List<ConfigurationSetting> items = clientStore.listSettingRevisons(settingSelector, store.getName());

        String etag = "";
        // If there is no result, etag will be considered empty.
        // A refresh will trigger once the selector returns a value.
        if (!items == null || !items.isEmpty()) {
            etag = items.get(0).getETag();
        }

        if (firstTimeMap.get(storeNameWithSuffix) == null) {
            storeEtagMap.put(storeNameWithSuffix, etag);
            firstTimeMap.put(storeNameWithSuffix, false);
            return false;
        }

        if (!etag.equals(storeEtagMap.get(storeNameWithSuffix))) {
            LOGGER.trace("Some keys in store [{}] matching [{}] is updated, will send refresh event.",
                    store.getName(), watchedKeyNames);
            storeEtagMap.put(storeNameWithSuffix, etag);
            if (eventDataInfo.isEmpty()) {
                eventDataInfo = watchedKeyNames;
            } else {
                eventDataInfo += ", " + watchedKeyNames;
            }

            // Don't need to refresh here will be done in Property Source
            return true;
        }
        return false;
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh
     * is required.
     */
    class RefreshEventData {
        private static final String MSG_TEMPLATE = "Some keys matching %s has been updated since last check.";

        private final String message;

        public RefreshEventData(String prefix) {
            this.message = String.format(MSG_TEMPLATE, prefix);
        }

        public String getMessage() {
            return this.message;
        }
    }

    /**
     * Composite watched key names separated by comma, the key names is made up of:
     * prefix, context and key name pattern e.g., prefix: /config, context: /application,
     * watched key: my.watch.key will return: /config/application/my.watch.key
     *
     * The returned watched key will be one key pattern, one or multiple specific keys
     * e.g., 1) * 2) /application/abc* 3) /application/abc 4) /application/abc,xyz
     *
     * @param store the {@code store} for which to composite watched key names
     * @param storeContextsMap map storing store name and List of context key-value pair
     * @return the full name of the key mapping to the configuration store
     */
    private String watchedKeyNames(ConfigStore store, Map<String, List<String>> storeContextsMap) {
        String watchedKey = store.getWatchedKey().trim();
        List<String> contexts = storeContextsMap.get(store.getName());

        String watchedKeys = contexts.stream().map(ctx -> genKey(ctx, watchedKey))
                .collect(Collectors.joining(","));

        if (watchedKeys.contains(",") && watchedKeys.contains("*")) {
            // Multi keys including one or more key patterns is not supported by API, will
            // watch all keys(*) instead
            watchedKeys = "*";
        }

        return watchedKeys;
    }

    private String genKey(@NonNull String context, @Nullable String watchedKey) {
        String trimmedWatchedKey = StringUtils.hasText(watchedKey) ? watchedKey.trim() : "*";

        return String.format("%s%s", context, trimmedWatchedKey);
    }
}
