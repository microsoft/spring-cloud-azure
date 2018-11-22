/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.impl;

import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventHubOperation}.
 *
 * @author Warren Zhu
 */
@Slf4j
public class EventHubTemplate extends AbstractEventHubTemplate implements EventHubOperation {

    // Use this concurrent map as set since no concurrent set which has putIfAbsent
    private final ConcurrentMap<Tuple<String, String>, Boolean> subscribedNameAndGroup = new ConcurrentHashMap<>();

    public EventHubTemplate(EventHubClientFactory clientFactory) {
        super(clientFactory);
        log.info("Started EventHubTemplate with properties: {}", buildPropertiesMap());
    }

    @Override
    public boolean subscribe(String destination, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> messagePayloadType) {
        if (subscribedNameAndGroup.putIfAbsent(Tuple.of(destination, consumerGroup), true) == null) {
            this.register(destination, consumerGroup,
                    new EventHubProcessor(consumer, messagePayloadType, getCheckpointConfig(), getMessageConverter()));
            log.info("Consumer subscribed to destination '{}' with consumer group '{}'", destination, consumerGroup);
            return true;
        }

        return false;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        if (subscribedNameAndGroup.remove(Tuple.of(destination, consumerGroup), true)) {
            unregister(destination, consumerGroup);
            log.info("Consumer unsubscribed from destination '{}' with consumer group '{}'", destination,
                    consumerGroup);
            return true;
        }

        return false;
    }
}
