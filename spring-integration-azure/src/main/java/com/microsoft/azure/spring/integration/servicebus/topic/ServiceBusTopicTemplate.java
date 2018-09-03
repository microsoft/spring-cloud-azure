/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.ISubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusTopicOperation}.
 *
 * @author Warren Zhu
 */
public class ServiceBusTopicTemplate extends ServiceBusTemplate<ServiceBusTopicClientFactory>
        implements ServiceBusTopicOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTopicTemplate.class);
    private Set<Tuple<String, String>> nameAndConsumerGroups = Sets.newConcurrentHashSet();

    public ServiceBusTopicTemplate(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public boolean subscribe(String destination, String consumerGroup, @NonNull Consumer<Message<?>> consumer,
            Class<?> payloadType) {
        Assert.hasText(destination, "destination can't be null or empty");

        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (nameAndConsumerGroups.contains(nameAndConsumerGroup)) {
            return false;
        }

        nameAndConsumerGroups.add(nameAndConsumerGroup);

        internalSubscribe(destination, consumerGroup, consumer, payloadType);
        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        //TODO: unregister message handler but service bus sdk unsupported

        return nameAndConsumerGroups.remove(Tuple.of(destination, consumerGroup));
    }

    @SuppressWarnings("unchecked")
    protected void internalSubscribe(String name, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> payloadType) {
        ISubscriptionClient subscriptionClient =
                this.senderFactory.getSubscriptionClientCreator().apply(Tuple.of(name, consumerGroup));

        try {
            subscriptionClient
                    .registerMessageHandler(new TopicMessageHandler(consumer, payloadType, subscriptionClient));
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register topic message handler", e);
            throw new ServiceBusRuntimeException("Failed to register topic message handler", e);
        }
    }

    protected class TopicMessageHandler<U> extends ServiceBusMessageHandler<U>{
        private final ISubscriptionClient subscriptionClient;

        public TopicMessageHandler(Consumer<Message<U>> consumer, Class<U> payloadType,
                ISubscriptionClient subscriptionClient) {
            super(consumer, payloadType);
            this.subscriptionClient = subscriptionClient;
        }

        @Override
        protected CompletableFuture<Void> success(UUID uuid) {
            return subscriptionClient.completeAsync(uuid);
        }

        @Override
        protected CompletableFuture<Void> failure(UUID uuid) {
            return subscriptionClient.abandonAsync(uuid);
        }
    }
}
