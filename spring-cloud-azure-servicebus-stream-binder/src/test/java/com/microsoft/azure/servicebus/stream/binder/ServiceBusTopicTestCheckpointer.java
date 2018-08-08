/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.spring.integration.core.Checkpointer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServiceBusTopicTestCheckpointer implements Checkpointer<UUID> {

    @Override
    public CompletableFuture<Void> checkpoint() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }

    @Override
    public CompletableFuture<Void> checkpoint(UUID uuid) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }
}
