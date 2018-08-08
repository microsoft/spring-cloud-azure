/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.spring.integration.core.Checkpointer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServiceBusQueueCheckpointer implements Checkpointer<UUID> {
    private final IQueueClient queueClient;

    public ServiceBusQueueCheckpointer(IQueueClient queueClient) {
        this.queueClient = queueClient;
    }

    @Override
    public CompletableFuture<Void> checkpoint() {
        // Service bus unsupported
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> checkpoint(UUID messageKey) {
        return this.queueClient.completeAsync(messageKey);
    }
}
