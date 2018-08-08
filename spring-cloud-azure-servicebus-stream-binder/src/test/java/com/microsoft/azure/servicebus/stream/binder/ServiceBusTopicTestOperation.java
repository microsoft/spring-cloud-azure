/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServiceBusTopicTestOperation implements ServiceBusTopicOperation {
    private final Map<String, Map<String, Consumer<Iterable<IMessage>>>> consumerMap = new ConcurrentHashMap<>();
    private final Map<String, List<IMessage>> serviceBusTopicsByName = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> sendAsync(String serviceBusTopicName, IMessage message,
            PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        serviceBusTopicsByName.putIfAbsent(serviceBusTopicName, new LinkedList<>());
        serviceBusTopicsByName.get(serviceBusTopicName).add(message);
        consumerMap.putIfAbsent(serviceBusTopicName, new ConcurrentHashMap<>());
        consumerMap.get(serviceBusTopicName).values().forEach(c -> c.accept(Collections.singleton(message)));
        future.complete(null);
        return future;
    }

    @Override
    public boolean subscribe(String destination, Consumer<Iterable<IMessage>> consumer, String consumerGroup) {
        consumerMap.putIfAbsent(destination, new ConcurrentHashMap<>());
        consumerMap.get(destination).put(consumerGroup, consumer);
        serviceBusTopicsByName.putIfAbsent(destination, new LinkedList<>());

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, Consumer<Iterable<IMessage>> consumer, String consumerGroup) {
        consumerMap.get(destination).remove(consumerGroup);
        return true;
    }

    @Override
    public Checkpointer<UUID> getCheckpointer(String destination, String consumerGroup) {
        return new ServiceBusTopicTestCheckpointer();
    }
}

