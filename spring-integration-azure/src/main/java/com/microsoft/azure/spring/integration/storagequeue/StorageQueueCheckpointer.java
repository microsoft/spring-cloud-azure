/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storagequeue;

import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

import java.util.concurrent.CompletableFuture;

public class StorageQueueCheckpointer implements Checkpointer<CloudQueueMessage> {
    private final CloudQueue cloudQueue;

    public StorageQueueCheckpointer(CloudQueue cloudQueue) {
        this.cloudQueue = cloudQueue;
    }

    @Override
    public CompletableFuture<Void> checkpoint() {
        // Storage queue unsupported
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> checkpoint(CloudQueueMessage cloudQueueMessage) {
        // Storage queue unsupported
        throw new UnsupportedOperationException();
    }
}
