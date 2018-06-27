/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventHubTestOperation implements EventHubOperation {
    private final Map<String, Map<String, Consumer<Iterable<EventData>>>> consumerMap = new ConcurrentHashMap<>();
    private final Map<String, List<EventData>> eventHubsByName = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> sendAsync(String eventHubName, EventData message,
            PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        eventHubsByName.putIfAbsent(eventHubName, new LinkedList<>());
        eventHubsByName.get(eventHubName).add(message);
        consumerMap.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
        consumerMap.get(eventHubName).values().forEach(c -> c.accept(Collections.singleton(message)));
        future.complete(null);
        return future;
    }

    @Override
    public boolean subscribe(String destination, Consumer<Iterable<EventData>> consumer, String consumerGroup) {
        consumerMap.putIfAbsent(destination, new ConcurrentHashMap<>());
        consumerMap.get(destination).put(consumerGroup, consumer);
        eventHubsByName.putIfAbsent(destination, new LinkedList<>());

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, Consumer<Iterable<EventData>> consumer, String consumerGroup) {
        consumerMap.get(destination).remove(consumerGroup);
        return true;
    }

    @Override
    public Checkpointer<EventData> getCheckpointer(String destination, String consumerGroup) {
        return new EventHubTestCheckpointer();
    }
}

