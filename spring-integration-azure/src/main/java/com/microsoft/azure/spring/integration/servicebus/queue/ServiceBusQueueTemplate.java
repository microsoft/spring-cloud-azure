/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
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
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 */
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
        implements ServiceBusQueueOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private final Set<String> subscribedQueues = Sets.newConcurrentHashSet();

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination, @NonNull Consumer<Message<?>> consumer, Class<?> targetPayloadClass) {
        Assert.hasText(destination, "destination can't be null or empty");

        if (subscribedQueues.contains(destination)) {
            return false;
        }

        subscribedQueues.add(destination);
        IQueueClient queueClient = this.senderFactory.getQueueClientCreator().apply(destination);

        Function<UUID, CompletableFuture<Void>> success = queueClient::completeAsync;
        Function<UUID, CompletableFuture<Void>> failure = queueClient::abandonAsync;

        try {

            queueClient.registerMessageHandler(
                    new ServiceBusMessageHandler(consumer, targetPayloadClass, success, failure));
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register message handler", e);
            throw new ServiceBusRuntimeException("Failed to register message handler", e);
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination) {

        //TODO: unregister message handler but service bus sdk unsupported

        return subscribedQueues.remove(destination);
    }
}
