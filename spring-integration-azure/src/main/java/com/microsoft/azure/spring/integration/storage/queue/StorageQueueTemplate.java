/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.Memoizer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueFactory;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.springframework.util.Assert;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StorageQueueTemplate implements StorageQueueOperation {
    private final StorageQueueFactory storageQueueFactory;
    private final Function<String, Checkpointer<CloudQueueMessage>> checkpointGetter =
            Memoizer.memoize(this::createCheckpointer);
    private static final int DEFAULT_VISIBILITY_MESSAGE_TIMEOUT_IN_SECONDS = 30;

    public StorageQueueTemplate(StorageQueueFactory storageQueueFactory) {
        this.storageQueueFactory = storageQueueFactory;
    }

    private CloudQueue getOrCreateQueue(String destination) {
        Assert.hasText(destination, "destination can't be null or empty");
        return storageQueueFactory.getQueueCreator().apply(destination);
    }

    @Override
    public CompletableFuture<Void> sendAsync(String destination, CloudQueueMessage cloudQueueMessage,
                                             PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            CloudQueue cloudQueue = getOrCreateQueue(destination);
            try {
                cloudQueue.addMessage(cloudQueueMessage);
            } catch (StorageException e) {
                throw new StorageQueueRuntimeException("Failed to add message to cloud queue", e);
            }
        });
        return completableFuture;
    }

    @Override
    public CompletableFuture<CloudQueueMessage> receiveAsync(String destination) {
        return this.receiveAsync(destination, DEFAULT_VISIBILITY_MESSAGE_TIMEOUT_IN_SECONDS);
    }

    @Override
    public CompletableFuture<CloudQueueMessage> receiveAsync(String destination, int visibilityTimeoutInSeconds) {
        CompletableFuture<CloudQueueMessage> completableFuture = CompletableFuture.supplyAsync(() -> {
            CloudQueue cloudQueue = getOrCreateQueue(destination);
            try {
                return cloudQueue.retrieveMessage(visibilityTimeoutInSeconds, null, null);
            } catch (StorageException e) {
                throw new StorageQueueRuntimeException("Failed to peek message from cloud queue", e);
            }
        });
        return completableFuture;
    }

    @Override
    public Checkpointer<CloudQueueMessage> getCheckpointer(String destination) {
        return checkpointGetter.apply(destination);
    }

    private Checkpointer<CloudQueueMessage> createCheckpointer(String destination) {
        return new StorageQueueCheckpointer(this.storageQueueFactory.getQueueCreator().apply(destination));
    }
}
