/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhubs.EventData;
import eventhub.core.EventHubOperation;
import eventhub.integration.inbound.Checkpointer;
import eventhub.integration.inbound.Subscriber;
import eventhub.integration.outbound.PartitionSupplier;

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
    public CompletableFuture<Void> sendAsync(String eventHubName, EventData data, PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        eventHubsByName.putIfAbsent(eventHubName, new LinkedList<>());
        eventHubsByName.get(eventHubName).add(data);
        consumerMap.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
        consumerMap.get(eventHubName).values().forEach(c -> c.accept(Collections.singleton(data)));
        future.complete(null);
        return future;
    }

    @Override
    public Subscriber<EventData> subscribe(String eventHubName, String consumerGroup) {
        return new TestSubscriber(eventHubName, consumerGroup);
    }

    private class TestSubscriber implements Subscriber<EventData> {
        private final String eventHubName;
        private final String consumerGroup;

        private TestSubscriber(String eventHubName, String consumerGroup) {
            this.eventHubName = eventHubName;
            this.consumerGroup = consumerGroup;
        }

        @Override
        public void subscribe(Consumer<Iterable<EventData>> consumer) {
            consumerMap.putIfAbsent(eventHubName, new ConcurrentHashMap<>());
            consumerMap.get(eventHubName).put(consumerGroup, consumer);
            eventHubsByName.putIfAbsent(eventHubName, new LinkedList<>());
        }

        @Override
        public void unsubscribe() {
            consumerMap.get(eventHubName).remove(consumerGroup);
        }

        @Override
        public Checkpointer<EventData> getCheckpointer() {
            return new EventHubTestCheckpointer();
        }
    }
}

