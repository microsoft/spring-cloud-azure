/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.support;

import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import lombok.Setter;
import org.springframework.messaging.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InMemoryOperation<T> implements SendOperation, SubscribeByGroupOperation {
    private final Map<String, Map<String, Consumer<Message<?>>>> consumerByNameAndGroup = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Class<?>>> payloadTypeByNameAndGroup = new ConcurrentHashMap<>();
    private final Map<String, List<T>> topicsByName = new ConcurrentHashMap<>();

    @Setter
    private StartPosition startPosition = StartPosition.LATEST;

    @Setter
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    private final Class<T> azureMessageType;

    private final AzureMessageConverter<T> messageConverter;

    public InMemoryOperation(Class<T> azureMessageType, AzureMessageConverter<T> messageConverter) {
        this.azureMessageType = azureMessageType;
        this.messageConverter = messageConverter;
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String topicName, Message<U> message,
            PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        T azureMessage = messageConverter.fromMessage(message, azureMessageType);

        topicsByName.putIfAbsent(topicName, new LinkedList<>());
        topicsByName.get(topicName).add(azureMessage);
        consumerByNameAndGroup.putIfAbsent(topicName, new ConcurrentHashMap<>());
        consumerByNameAndGroup.get(topicName).forEach((k, v) -> v.accept(messageConverter.toMessage(azureMessage,
                payloadTypeByNameAndGroup.get(topicName).get(k))));

        future.complete(null);
        return future;
    }

    @Override
    public boolean subscribe(String topicName, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> payloadClass) {
        consumerByNameAndGroup.putIfAbsent(topicName, new ConcurrentHashMap<>());
        consumerByNameAndGroup.get(topicName).put(consumerGroup, consumer);
        payloadTypeByNameAndGroup.putIfAbsent(topicName, new ConcurrentHashMap<>());
        payloadTypeByNameAndGroup.get(topicName).put(consumerGroup, payloadClass);
        topicsByName.putIfAbsent(topicName, new LinkedList<>());

        if (this.startPosition == StartPosition.EARLISET) {
            topicsByName.get(topicName).forEach(e -> consumer.accept(messageConverter.toMessage(e, payloadClass)));
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        consumerByNameAndGroup.get(destination).remove(consumerGroup);
        return true;
    }
}

