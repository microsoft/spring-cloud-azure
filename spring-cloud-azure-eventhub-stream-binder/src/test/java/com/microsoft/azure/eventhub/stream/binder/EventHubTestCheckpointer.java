/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.Checkpointer;

import java.util.concurrent.CompletableFuture;

public class EventHubTestCheckpointer implements Checkpointer<EventData> {

    @Override
    public CompletableFuture<Void> checkpoint() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }

    @Override
    public CompletableFuture<Void> checkpoint(EventData bytes) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }
}
