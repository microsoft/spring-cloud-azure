/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Default implementation of {@link IMessageHandler} to handle Service Bus {@link IMessage}
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageHandler implements IMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageHandler.class);
    private final Consumer<IMessage> consumer;

    public ServiceBusMessageHandler(@NonNull Consumer<IMessage> consumer) {
        this.consumer = consumer;
    }

    @Override
    public CompletableFuture<Void> onMessageAsync(IMessage message) {
        consumer.accept(message);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void notifyException(Throwable exception, ExceptionPhase phase) {
        LOGGER.error(String.format("Exception encountered in phase %s", phase), exception);
    }
}
