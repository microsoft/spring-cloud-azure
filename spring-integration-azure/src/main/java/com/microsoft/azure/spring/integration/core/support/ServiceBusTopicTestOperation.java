/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.ISubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServiceBusTopicTestOperation extends ServiceBusTopicTemplate {
    private final Multimap<String, IMessage> topicsByName = ArrayListMultimap.create();
    private final Map<String, Map<String, ServiceBusMessageHandler<?>>> handlersByNameAndGroup =
            new ConcurrentHashMap<>();

    public ServiceBusTopicTestOperation(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        IMessage azureMessage = getMessageConverter().fromMessage(message, IMessage.class);

        topicsByName.put(name, azureMessage);
        handlersByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());
        handlersByNameAndGroup.get(name).values().forEach(c -> c.onMessageAsync(azureMessage));

        future.complete(null);
        return future;
    }

    @Override
    protected void internalSubscribe(String name, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> payloadType) {
        ISubscriptionClient subscriptionClient =
                this.senderFactory.getSubscriptionClientCreator().apply(Tuple.of(name, consumerGroup));

        Function<UUID, CompletableFuture<Void>> success = subscriptionClient::completeAsync;
        Function<UUID, CompletableFuture<Void>> failure = subscriptionClient::abandonAsync;

        ServiceBusMessageHandler handler = new ServiceBusMessageHandler(consumer, payloadType, success, failure);

        try {
            subscriptionClient.registerMessageHandler(handler);
        } catch (ServiceBusException | InterruptedException e) {
            throw new ServiceBusRuntimeException("Failed to internalSubscribe message handler", e);
        }

        handlersByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());
        handlersByNameAndGroup.get(name).put(consumerGroup, handler);
    }

    @Override
    public boolean unsubscribe(String name, String consumerGroup) {
        handlersByNameAndGroup.get(name).remove(consumerGroup);
        return true;
    }
}

