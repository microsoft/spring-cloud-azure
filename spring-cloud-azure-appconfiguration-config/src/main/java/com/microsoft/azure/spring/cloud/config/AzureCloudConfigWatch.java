/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.QueryField;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;

public class AzureCloudConfigWatch implements ApplicationEventPublisherAware, SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCloudConfigWatch.class);
    private final ConfigServiceOperations configOperations;
    private final Map<String, String> storeEtagMap = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ApplicationEventPublisher publisher;
    private ScheduledFuture<?> watchFuture;
    private final AzureCloudConfigProperties properties;
    private final Map<String, Boolean> firstTimeMap = new ConcurrentHashMap<>();
    private final List<ConfigStore> configStores;
    private final Map<String, List<String>> storeContextsMap;
    
    private static final String CONFIGURATION_STORE = "_configuration";
    private static final String FEATURE_STORE = "_feature";
    private static final String FEATURE_STORE_WATCH_KEY = "*appconfig*";

    public AzureCloudConfigWatch(ConfigServiceOperations operations, AzureCloudConfigProperties properties,
                                 TaskScheduler scheduler, Map<String, List<String>> storeContextsMap) {
        this.configOperations = operations;
        this.properties = properties;
        this.taskScheduler = scheduler;
        this.configStores = properties.getStores();
        this.storeContextsMap = storeContextsMap;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(this::watchConfigKeyValues,
                    this.properties.getWatch().getDelay());
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
            this.watchFuture.cancel(true);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    public void watchConfigKeyValues() {
        if (!this.running.get()) {
            return;
        }

        for (ConfigStore configStore : configStores) {
            if (needRefreshConfiguration(configStore) || needRefreshFeatureFlag(configStore)) {
                break;
            }
        }
    }

    private boolean needRefreshConfiguration(ConfigStore store) {
        String watchedKeyNames = watchedKeyNames(store, storeContextsMap);
        return needRefresh(store, CONFIGURATION_STORE, watchedKeyNames);
    }
    
    private boolean needRefreshFeatureFlag(ConfigStore store) {
        return needRefresh(store, FEATURE_STORE, FEATURE_STORE_WATCH_KEY);
    }
    
    private boolean needRefresh(ConfigStore store, String storeName, String watchedKeyNames) {
        QueryOptions options = new QueryOptions().withKeyNames(watchedKeyNames)
                .withLabels(store.getLabels()).withFields(QueryField.ETAG).withRange(0, 0);

        List<KeyValueItem> keyValueItems = configOperations.getRevisions(store.getName(), options);
        
        if (keyValueItems.isEmpty()) {
            return false;
        }

        String etag = keyValueItems.get(0).getEtag();
        if (firstTimeMap.get(store.getName() + storeName) == null) {
            storeEtagMap.put(store.getName() + storeName, etag);
            firstTimeMap.put(store.getName() + storeName, false);
            return false;
        }

        if (!etag.equals(storeEtagMap.get(store.getName() + storeName))) {
            storeEtagMap.put(store.getName() + storeName, etag);
            RefreshEventData eventData = new RefreshEventData("*appconfig*");
            publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
            return true;
        }

        return false;
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh is required.
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
     * Composite watched key names separated by comma, the key names is made up of: prefix, context and key name pattern
     * e.g., prefix: /config, context: /application, watched key: my.watch.key
     *      will return: /config/application/my.watch.key
     *
     * The returned watched key will be one key pattern, one or multiple specific keys
     * e.g., 1) *
     *       2) /application/abc*
     *       3) /application/abc
     *       4) /application/abc,xyz
     *
     * @param store the {@code store} for which to composite watched key names
     * @param storeContextsMap map storing store name and List of context key-value pair
     * @return the full name of the key mapping to the configuration store
     */
    private String watchedKeyNames(ConfigStore store, Map<String, List<String>> storeContextsMap) {
        String prefix = store.getPrefix();
        String watchedKey = store.getWatchedKey().trim();
        List<String> contexts = storeContextsMap.get(store.getName());

        String watchedKeys = contexts.stream().map(ctx -> genKey(prefix, ctx, watchedKey))
                .collect(Collectors.joining(","));

        if (watchedKeys.contains(",") && watchedKeys.contains("*")) {
            // Multi keys including one or more key patterns is not supported by API, will watch all keys(*) instead
            watchedKeys = "*";
        }

        return watchedKeys;
    }

    private String genKey(@Nullable String prefix, @NonNull String context, @Nullable String watchedKey) {
        String trimmedWatchedKey = StringUtils.hasText(watchedKey) ? watchedKey.trim() : "*";
        String trimmedPrefix = StringUtils.hasText(prefix) ? prefix.trim() : "";

        return String.format("%s%s%s", trimmedPrefix, context, trimmedWatchedKey);
    }
}
