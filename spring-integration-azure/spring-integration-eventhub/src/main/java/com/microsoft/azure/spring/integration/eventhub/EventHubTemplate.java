/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import org.springframework.messaging.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventHubOperation}.
 *
 * @author Warren Zhu
 */
public class EventHubTemplate extends AbstractEventHubTemplate implements EventHubOperation {

    // Use this concurrent map as set since no concurrent set which has putIfAbsent
    private final ConcurrentMap<Tuple<String, String>, Boolean> subscribedNameAndGroup = new ConcurrentHashMap<>();

    public EventHubTemplate(EventHubClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public boolean subscribe(String destination, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> messagePayloadType) {
        if (subscribedNameAndGroup.putIfAbsent(Tuple.of(destination, consumerGroup), true) == null) {
            this.register(destination, consumerGroup,
                    new EventHubProcessor(consumer, messagePayloadType, getCheckpointConfig(), getMessageConverter()));
            return true;
        }

        return false;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        if (subscribedNameAndGroup.remove(Tuple.of(destination, consumerGroup), true)) {
            unregister(destination, consumerGroup);
            return true;
        }

        return false;
    }
}
