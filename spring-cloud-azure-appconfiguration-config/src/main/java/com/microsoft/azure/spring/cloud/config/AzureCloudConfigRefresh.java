/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.Constants.CONFIGURATION_SUFFIX;
import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_STORE_WATCH_KEY;
import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_SUFFIX;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

public class AzureCloudConfigRefresh implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCloudConfigRefresh.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ApplicationEventPublisher publisher;

    private final List<ConfigStore> configStores;

    private final Map<String, List<String>> storeContextsMap;

    private Duration delay;

    private ClientStore clientStore;

    private Date lastCheckedTime;

    private String eventDataInfo;

    public AzureCloudConfigRefresh(AzureCloudConfigProperties properties, Map<String, List<String>> storeContextsMap,
            ClientStore clientStore) {
        this.configStores = properties.getStores();
        this.storeContextsMap = storeContextsMap;
        this.delay = properties.getCacheExpiration();
        this.lastCheckedTime = new Date();
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
        return CompletableFuture.supplyAsync(() -> refreshStores());
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed.
     * If any store has a value that needs to be updated a refresh event is called after
     * every store is checked.
     * @return If a refresh event is called.
     */
    private boolean refreshStores() {
        boolean willRefresh = false;
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
                        String watchedKeyNames = clientStore.watchedKeyNames(configStore, storeContextsMap);
                        willRefresh = refresh(configStore, CONFIGURATION_SUFFIX, watchedKeyNames) ? true
                                : willRefresh;
                        // Refresh Feature Flags
                        willRefresh = refresh(configStore, FEATURE_SUFFIX, FEATURE_STORE_WATCH_KEY) ? true
                                : willRefresh;
                    }
                    // Resetting last Checked date to now.
                    lastCheckedTime = new Date();
                }
                if (willRefresh) {
                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                }
            } finally {
                running.set(false);
            }
        }
        return willRefresh;
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
        String storeNameWithSuffix = store.getEndpoint() + storeSuffix;
        SettingSelector settingSelector = new SettingSelector().setKeyFilter(watchedKeyNames)
                .setLabelFilter(StringUtils.arrayToCommaDelimitedString(store.getLabels()));

        List<ConfigurationSetting> items = clientStore.listSettingRevisons(settingSelector, store.getEndpoint());

        String etag = "";
        // If there is no result, etag will be considered empty.
        // A refresh will trigger once the selector returns a value.
        if (items != null && !items.isEmpty()) {
            etag = items.get(0).getETag();
        }
        
        if (StateHolder.getState(storeNameWithSuffix) == null) {
            // Should never be the case as Property Source should set the state, but if
            // etag != null return true.
            if (etag != null) {
                return true;
            }
            return false;
        }
        
        if (!etag.equals(StateHolder.getState(storeNameWithSuffix).getETag())) {
            LOGGER.trace("Some keys in store [{}] matching [{}] is updated, will send refresh event.",
                    store.getEndpoint(), watchedKeyNames);
            if (this.eventDataInfo.isEmpty()) {
                this.eventDataInfo = watchedKeyNames;
            } else {
                this.eventDataInfo += ", " + watchedKeyNames;
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
}
