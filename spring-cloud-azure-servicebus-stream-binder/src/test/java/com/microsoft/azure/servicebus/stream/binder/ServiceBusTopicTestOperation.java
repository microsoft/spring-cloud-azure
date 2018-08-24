/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.springframework.messaging.Message;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServiceBusTopicTestOperation extends ServiceBusTopicTemplate implements ServiceBusTopicOperation {
    private final Map<String, Map<String, Consumer<IMessage>>> consumerMap = new ConcurrentHashMap<>();
    private final Map<String, List<IMessage>> serviceBusTopicsByName = new ConcurrentHashMap<>();

    public ServiceBusTopicTestOperation(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String serviceBusTopicName, Message<T> message,
            PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        serviceBusTopicsByName.putIfAbsent(serviceBusTopicName, new LinkedList<>());
        IMessage serviceBusMessage = toServiceBusMessage(message);

        serviceBusTopicsByName.get(serviceBusTopicName).add(serviceBusMessage);
        consumerMap.putIfAbsent(serviceBusTopicName, new ConcurrentHashMap<>());
        consumerMap.get(serviceBusTopicName).values().forEach(c -> c.accept(serviceBusMessage));
        future.complete(null);
        return future;
    }

    @Override
    public boolean subscribe(String destination, Consumer<IMessage> consumer, String consumerGroup) {
        consumerMap.putIfAbsent(destination, new ConcurrentHashMap<>());
        consumerMap.get(destination).put(consumerGroup, consumer);
        serviceBusTopicsByName.putIfAbsent(destination, new LinkedList<>());

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        consumerMap.get(destination).remove(consumerGroup);
        return true;
    }

    @Override
    public Checkpointer<UUID> getCheckpointer(String destination, String consumerGroup) {
        return new ServiceBusTopicTestCheckpointer();
    }
}

