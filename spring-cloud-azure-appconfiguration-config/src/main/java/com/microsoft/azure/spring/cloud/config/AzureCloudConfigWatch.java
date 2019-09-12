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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.QueryField;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;

public class AzureCloudConfigWatch implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCloudConfigWatch.class);

    private final ConfigServiceOperations configOperations;

    private final Map<String, String> storeEtagMap = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ApplicationEventPublisher publisher;

    private final Map<String, Boolean> firstTimeMap = new ConcurrentHashMap<>();

    private final List<ConfigStore> configStores;

    private final Map<String, List<String>> storeContextsMap;

    private static final String CONFIGURATION_SUFFIX = "_configuration";

    private static final String FEATURE_SUFFIX = "_feature";

    private static final String FEATURE_STORE_WATCH_KEY = ".appconfig*";

    private Duration delay;

    private PropertyCache propertyCache;

    public AzureCloudConfigWatch(ConfigServiceOperations operations, AzureCloudConfigProperties properties,
            Map<String, List<String>> storeContextsMap, PropertyCache propertyCache) {
        this.configOperations = operations;
        this.configStores = properties.getStores();
        this.storeContextsMap = storeContextsMap;
        this.delay = properties.getWatch().getDelay();
        this.propertyCache = propertyCache;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * Checks configurations to see if they are no longer cached. If they are no longer
     * cached they are updated.
     * 
     * @return CompletableFuture<Boolean> will return true if a refresh event was triggered.
     */
    public CompletableFuture<Boolean> refreshConfigurations() {
        return CompletableFuture.supplyAsync(this::refreshConfigStores);
    }

    public Boolean refreshConfigStores() {
        Boolean refreshed = false;
        if (this.running.compareAndSet(false, true)) {
            for (ConfigStore configStore : configStores) {
                if (propertyCache.findNonCachedKeys(delay, configStore.getName()).size() > 0) {
                    String watchedKeyNames = watchedKeyNames(configStore, storeContextsMap);
                    refresh(configStore, CONFIGURATION_SUFFIX, watchedKeyNames);

                    // Refresh Feature Flags
                    if (propertyCache.getRefreshKeys(configStore.getName(), FEATURE_STORE_WATCH_KEY.replace("*", ""))
                            .size() > 0) {
                        refreshed = refresh(configStore, FEATURE_SUFFIX, FEATURE_STORE_WATCH_KEY) ? true : refreshed;
                    }
                }
            }
            this.running.set(false);
        }
        return refreshed;
    }

    /**
     * Checks un-cached items for etag changes. If they have changed a RefreshEventData is
     * published.
     * 
     * @param store the {@code store} for which to composite watched key names
     * @param storeSuffix Suffix used to distinguish between Settings and Features
     * @param watchedKeyNames Key used to check if refresh should occur
     */
    private Boolean refresh(ConfigStore store, String storeSuffix, String watchedKeyNames) {
        Boolean refreshed = false;
        String storeName = store.getName() + storeSuffix;
        QueryOptions options = new QueryOptions().withKeyNames(watchedKeyNames)
                .withLabels(store.getLabels()).withFields(QueryField.ETAG).withRange(0, 0);

        List<KeyValueItem> keyValueItems = configOperations.getRevisions(store.getName(), options);

        if (keyValueItems.isEmpty()) {
            return refreshed;
        }

        String etag = keyValueItems.get(0).getEtag();
        if (firstTimeMap.get(storeName) == null) {
            storeEtagMap.put(storeName, etag);
            firstTimeMap.put(storeName, false);
            propertyCache.updateRefreshCacheTime(store.getName(), watchedKeyNames, delay);
        }

        if (!etag.equals(storeEtagMap.get(storeName))) {
            Date date = new Date();
            String watchedKeyNamesPrefix = watchedKeyNames.replace("*", "");

            // Checks all cached items to see if they have been updated
            List<String> refreshKeys = propertyCache.getRefreshKeys(store.getName());
            // RefreshKeyIndex is the current refresh key being checked. If not needing
            // refresh it is removed from the list.
            for (int refreshKeyIndex = 0; refreshKeyIndex < refreshKeys.size(); refreshKeyIndex++) {
                String refreshKey = refreshKeys.get(refreshKeyIndex);
                if (refreshKey.toLowerCase().startsWith(watchedKeyNamesPrefix.toLowerCase())) {

                    storeEtagMap.put(storeName, etag);
                    options = new QueryOptions().withKeyNames(refreshKey)
                            .withLabels(store.getLabels()).withFields(QueryField.ETAG).withRange(0, 0);

                    keyValueItems = configOperations.getRevisions(store.getName(), options);

                    if (keyValueItems.isEmpty() || keyValueItems.get(0).getEtag()
                            .equals(propertyCache.getCachedEtag(refreshKey))) {
                        refreshKeys = propertyCache.updateRefreshCacheTimeForKey(store.getName(), refreshKey, date);
                        refreshKeyIndex--;
                    }
                }
            }

            if (refreshKeys.size() > 0) {
                LOGGER.trace("Some keys in store [{}] matching [{}] is updated, will send refresh event.",
                        store.getName(), watchedKeyNames);
                storeEtagMap.put(storeName, etag);
                RefreshEventData eventData = new RefreshEventData(watchedKeyNames);
                publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));

                // Don't need to refresh here will be done in Property Source
                refreshed = true;
            }
        }
        propertyCache.updateRefreshCacheTime(store.getName(), watchedKeyNames, delay);
        return refreshed;
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
