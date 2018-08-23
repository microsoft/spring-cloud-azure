/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.StartPosition;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import lombok.Setter;
import org.springframework.messaging.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServiceBusTopicTestOperation extends ServiceBusTopicTemplate implements ServiceBusTopicOperation {
    private final Map<String, Map<String, Consumer<Message<?>>>> consumerMap = new ConcurrentHashMap<>();
    private final Map<String, List<IMessage>> topicsByName = new ConcurrentHashMap<>();

    @Setter
    private StartPosition startPosition = StartPosition.LATEST;

    public ServiceBusTopicTestOperation(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String topicName, Message<T> message,
            PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        IMessage serviceBusMessage = getMessageConverter().fromMessage(message, IMessage.class);

        topicsByName.putIfAbsent(topicName, new LinkedList<>());
        topicsByName.get(topicName).add(serviceBusMessage);
        consumerMap.putIfAbsent(topicName, new ConcurrentHashMap<>());
        consumerMap.get(topicName).values()
                   .forEach(c -> c.accept(getMessageConverter().toMessage(serviceBusMessage, byte[].class)));

        future.complete(null);
        return future;
    }

    @Override
    public <T> boolean subscribe(String topicName, String consumerGroup, Consumer<Message<?>> consumer,
            Class<T> payloadClass) {
        consumerMap.putIfAbsent(topicName, new ConcurrentHashMap<>());
        consumerMap.get(topicName).put(consumerGroup, consumer);
        topicsByName.putIfAbsent(topicName, new LinkedList<>());

        if (this.startPosition == StartPosition.EARLISET) {
            topicsByName.get(topicName).forEach(e -> consumer.accept(getMessageConverter().toMessage(e, payloadClass)));
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        consumerMap.get(destination).remove(consumerGroup);
        return true;
    }
}

