/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.google.common.collect.Sets;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 */
@Slf4j
public class ServiceBusQueueTemplate extends ServiceBusTemplate<ServiceBusQueueClientFactory>
        implements ServiceBusQueueOperation {
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in queue '%s'";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in queue '%s' in %s mode";
    private final Set<String> subscribedQueues = Sets.newConcurrentHashSet();

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination, @NonNull Consumer<Message<?>> consumer,
            @NonNull Class<?> targetPayloadClass) {
        Assert.hasText(destination, "destination can't be null or empty");

        if (subscribedQueues.contains(destination)) {
            return false;
        }

        subscribedQueues.add(destination);

        internalSubscribe(destination, consumer, targetPayloadClass);

        return true;
    }

    @Override
    public boolean unsubscribe(String destination) {

        //TODO: unregister message handler but service bus sdk unsupported

        return subscribedQueues.remove(destination);
    }

    @SuppressWarnings("unchecked")
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {

        IQueueClient queueClient = this.senderFactory.getOrCreateClient(name);

        try {
            queueClient.registerMessageHandler(new QueueMessageHandler(consumer, payloadType, queueClient), options);
        } catch (ServiceBusException | InterruptedException e) {
            log.error("Failed to register queue message handler", e);
            throw new ServiceBusRuntimeException("Failed to register queue message handler", e);
        }
    }

    protected class QueueMessageHandler<U> extends ServiceBusMessageHandler<U> {
        private final IQueueClient queueClient;

        public QueueMessageHandler(Consumer<Message<U>> consumer, Class<U> payloadType, IQueueClient queueClient) {
            super(consumer, payloadType, ServiceBusQueueTemplate.this.getCheckpointConfig(), ServiceBusQueueTemplate
                    .this.getMessageConverter());
            this.queueClient = queueClient;
        }

        @Override
        protected CompletableFuture<Void> success(UUID uuid) {
            return queueClient.completeAsync(uuid);
        }

        @Override
        protected CompletableFuture<Void> failure(UUID uuid) {
            return queueClient.abandonAsync(uuid);
        }

        @Override
        protected String buildCheckpointFailMessage(Message<?> message) {
            return String.format(MSG_FAIL_CHECKPOINT, message, queueClient.getQueueName());
        }

        @Override
        protected String buildCheckpointSuccessMessage(Message<?> message) {
            return String.format(MSG_SUCCESS_CHECKPOINT, message, queueClient.getQueueName(),
                    getCheckpointConfig().getCheckpointMode());
        }
    }
}
