/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Azure service bus template to support send {@link Message} asynchronously
 *
 * @author Warren Zhu
 */
public class ServiceBusTemplate<T extends ServiceBusSenderFactory> implements SendOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTemplate.class);
    protected final T senderFactory;
    protected final MessageHandlerOptions options = new MessageHandlerOptions(1, false, Duration.ofMinutes(5));

    @Setter
    protected CheckpointMode checkpointMode = CheckpointMode.RECORD;

    @Getter
    @Setter
    protected ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();

    public ServiceBusTemplate(@NonNull T senderFactory) {
        this.senderFactory = senderFactory;
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String destination, @NonNull Message<U> message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        String partitionKey = getPartitionKey(partitionSupplier);
        IMessage serviceBusMessage = messageConverter.fromMessage(message, IMessage.class);

        if (StringUtils.hasText(partitionKey)) {
            serviceBusMessage.setPartitionKey(partitionKey);
        }

        return this.senderFactory.getOrCreateSender(destination).sendAsync(serviceBusMessage);
    }

    private String getPartitionKey(PartitionSupplier partitionSupplier) {
        if (partitionSupplier == null) {
            return "";
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionKey())) {
            return partitionSupplier.getPartitionKey();
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionId())) {
            return partitionSupplier.getPartitionId();
        }

        return "";
    }

    protected abstract class ServiceBusMessageHandler<U> implements IMessageHandler {
        private final Consumer<Message<U>> consumer;
        private final Class<U> payloadType;

        public ServiceBusMessageHandler(@NonNull Consumer<Message<U>> consumer, @NonNull Class<U> payloadType) {
            this.consumer = consumer;
            this.payloadType = payloadType;
        }

        @Override
        public CompletableFuture<Void> onMessageAsync(IMessage serviceBusMessage) {
            Map<String, Object> headers = new HashMap<>();

            Checkpointer checkpointer = new AzureCheckpointer(() -> this.success(serviceBusMessage.getLockToken()),
                    () -> this.failure(serviceBusMessage.getLockToken()));
            if (checkpointMode == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }

            Message<U> message =
                    messageConverter.toMessage(serviceBusMessage, new MessageHeaders(headers), payloadType);
            consumer.accept(message);

            if (checkpointMode == CheckpointMode.RECORD) {
                return checkpointer.success();
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void notifyException(Throwable exception, ExceptionPhase phase) {
            LOGGER.error(String.format("Exception encountered in phase %s", phase), exception);
        }

        protected abstract CompletableFuture<Void> success(UUID uuid);

        protected abstract CompletableFuture<Void> failure(UUID uuid);
    }

}
