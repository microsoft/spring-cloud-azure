/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AzureCloudConfigWatch implements ApplicationEventPublisherAware, SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureCloudConfigWatch.class);
    private final ConfigServiceOperations configOperations;
    private final LinkedHashMap<String, String> keyNameEtagMap = new LinkedHashMap<>();
    private final TaskScheduler taskScheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ApplicationEventPublisher publisher;
    private ScheduledFuture<?> watchFuture;
    private AzureCloudConfigProperties properties;
    private boolean firstTime = true;

    public AzureCloudConfigWatch(ConfigServiceOperations operations, AzureCloudConfigProperties properties,
                                 TaskScheduler scheduler) {
        this.configOperations = operations;
        this.properties = properties;
        this.taskScheduler = scheduler;
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
        if (this.running.get()) {
            String prefix = StringUtils.hasText(properties.getPrefix()) ? properties.getPrefix() + "*" : "*";
            List<KeyValueItem> keyValueItems = configOperations.getKeys(prefix, properties.getLabel());

            if (keyValueItems == null || keyValueItems.isEmpty()) {
                return;
            }

            LinkedHashMap<String, String> newKeyEtagMap = keyValueItems.stream()
                    .collect(Collectors.toMap(KeyValueItem::getKey, KeyValueItem::getEtag,
                            (v1, v2) -> v1, LinkedHashMap::new));
            if (firstTime) {
                keyNameEtagMap.putAll(newKeyEtagMap);
                firstTime = false;
                return;
            }

            Optional<String> changedKey = newKeyEtagMap.entrySet().stream()
                    .filter(e -> mapNotInclude(keyNameEtagMap, e.getKey(), e.getValue()))
                    .map(e -> e.getKey())
                    .findFirst();

            if (!firstTime && changedKey.isPresent()) {
                LOGGER.trace("Some key matching {} is updated, will send refresh event.", prefix);
                keyNameEtagMap.clear();
                keyNameEtagMap.putAll(newKeyEtagMap);
                RefreshEventData eventData = new RefreshEventData(prefix);
                publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
            }
        }
    }

    private boolean mapNotInclude(LinkedHashMap<String, String> map, String key, String value) {
        return !map.containsKey(key) || (map.get(key) != null && !map.get(key).equals(value));
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh is required.
     */
    class RefreshEventData {
        private static final String MSG_TEMPLATE = "Some key matching %s has been updated since last check.";
        private final String message;

        public RefreshEventData(String prefix) {
            this.message = String.format(MSG_TEMPLATE, prefix);
        }

        public String getMessage() {
            return this.message;
        }
    }
}
