/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
public abstract class ServiceBusMessageHandler<U> implements IMessageHandler {
    private final Consumer<Message<U>> consumer;
    private final Class<U> payloadType;
    private final CheckpointConfig checkpointConfig;
    private final ServiceBusMessageConverter messageConverter;

    @Override
    public CompletableFuture<Void> onMessageAsync(IMessage serviceBusMessage) {
        Map<String, Object> headers = new HashMap<>();

        Checkpointer checkpointer = new AzureCheckpointer(() -> this.success(serviceBusMessage.getLockToken()),
                () -> this.failure(serviceBusMessage.getLockToken()));
        if (checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        Message<U> message = messageConverter.toMessage(serviceBusMessage, new MessageHeaders(headers), payloadType);
        consumer.accept(message);

        if (checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
            return checkpointer.success().whenComplete((v, t) -> checkpointHandler(message, t));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void notifyException(Throwable exception, ExceptionPhase phase) {
        log.error(String.format("Exception encountered in phase %s", phase), exception);
    }

    protected abstract CompletableFuture<Void> success(UUID uuid);

    protected abstract CompletableFuture<Void> failure(UUID uuid);

    protected abstract String buildCheckpointFailMessage(Message<?> message);

    protected abstract String buildCheckpointSuccessMessage(Message<?> message);

    private void checkpointHandler(Message<?> message, Throwable t) {
        if (t != null) {
            if (log.isWarnEnabled()) {
                log.warn(buildCheckpointFailMessage(message), t);
            }
        } else if (log.isDebugEnabled()) {
            log.debug(buildCheckpointSuccessMessage(message));
        }
    }
}
