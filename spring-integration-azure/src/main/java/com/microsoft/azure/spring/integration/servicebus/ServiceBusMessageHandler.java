/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Default implementation of {@link IMessageHandler} to handle Service Bus {@link IMessage}
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageHandler implements IMessageHandler {
    private static final Log LOGGER = LogFactory.getLog(ServiceBusMessageHandler.class);
    private final Set<Consumer<Iterable<IMessage>>> consumers;

    public ServiceBusMessageHandler(@NonNull Set<Consumer<Iterable<IMessage>>> consumers) {
        this.consumers = consumers;
    }

    @Override
    public CompletableFuture<Void> onMessageAsync(IMessage message) {
        consumers.forEach(c -> c.accept(Collections.singleton(message)));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void notifyException(Throwable exception, ExceptionPhase phase) {
        LOGGER.error(String.format("Exception encountered in phase %s", phase), exception);
    }
}
