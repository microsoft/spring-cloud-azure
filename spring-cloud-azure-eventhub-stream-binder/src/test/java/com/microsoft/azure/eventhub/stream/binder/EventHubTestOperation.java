/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.EventHubTemplate;
import lombok.Setter;
import org.springframework.messaging.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventHubTestOperation extends EventHubTemplate implements EventHubOperation {
    private final Map<String, Map<String, Consumer<Message<?>>>> consumerMap = new ConcurrentHashMap<>();
    private final Map<String, List<EventData>> eventHubsByName = new ConcurrentHashMap<>();

    @Setter
    private StartPosition startPosition = StartPosition.LATEST;

    public EventHubTestOperation(EventHubClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String eventHubName, Message<T> message,
            PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        EventData eventData = getMessageConverter().fromMessage(message, EventData.class);

        eventHubsByName.putIfAbsent(eventHubName, new LinkedList<>());
        eventHubsByName.get(eventHubName).add(eventData);
        consumerMap.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
        consumerMap.get(eventHubName).values()
                   .forEach(c -> c.accept(getMessageConverter().toMessage(eventData, byte[].class)));

        future.complete(null);
        return future;
    }

    @Override
    public boolean subscribe(String eventHubName, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> payloadClass) {
        consumerMap.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
        consumerMap.get(eventHubName).put(consumerGroup, consumer);
        eventHubsByName.putIfAbsent(eventHubName, new LinkedList<>());

        if (this.startPosition == StartPosition.EARLISET) {
            eventHubsByName.get(eventHubName)
                           .forEach(e -> consumer.accept(getMessageConverter().toMessage(e, payloadClass)));
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        consumerMap.get(destination).remove(consumerGroup);
        return true;
    }
}

