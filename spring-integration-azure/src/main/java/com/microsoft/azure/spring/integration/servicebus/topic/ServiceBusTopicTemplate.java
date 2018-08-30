/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.google.common.collect.Sets;
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
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination, String consumerGroup, @NonNull Consumer<Message<?>> consumer,
            Class<?> payloadType) {
        Assert.hasText(destination, "destination can't be null or empty");

        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (nameAndConsumerGroups.contains(nameAndConsumerGroup)) {
            return false;
        }

        nameAndConsumerGroups.add(nameAndConsumerGroup);
        ISubscriptionClient subscriptionClient =
                this.senderFactory.getSubscriptionClientCreator().apply(nameAndConsumerGroup);

        Function<UUID, CompletableFuture<Void>> success = subscriptionClient::completeAsync;
        Function<UUID, CompletableFuture<Void>> failure = subscriptionClient::abandonAsync;

        try {
            subscriptionClient
                    .registerMessageHandler(new ServiceBusMessageHandler(consumer, payloadType, success, failure));
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register message handler", e);
            throw new ServiceBusRuntimeException("Failed to register message handler", e);
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        //TODO: unregister message handler but service bus sdk unsupported

        return nameAndConsumerGroups.remove(Tuple.of(destination, consumerGroup));
    }
}
