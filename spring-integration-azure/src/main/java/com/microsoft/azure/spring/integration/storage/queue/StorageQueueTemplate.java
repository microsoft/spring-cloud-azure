/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.Memoizer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StorageQueueTemplate implements StorageQueueOperation {
    private final StorageQueueClientFactory storageQueueClientFactory;
    private final Function<String, Checkpointer<CloudQueueMessage>> checkpointerGetter =
            Memoizer.memoize(this::createCheckpointer);
    private static final int DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS = 30;
    private int visibilityTimeoutInSeconds;

    public StorageQueueTemplate(@NonNull StorageQueueClientFactory storageQueueClientFactory) {
        this.storageQueueClientFactory = storageQueueClientFactory;
        visibilityTimeoutInSeconds = DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS;
    }

    @Override
    public CompletableFuture<Void> sendAsync(String destination, CloudQueueMessage cloudQueueMessage,
                                             PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            CloudQueue cloudQueue = storageQueueClientFactory.getQueueCreator().apply(destination);
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
        return this.receiveAsync(destination, visibilityTimeoutInSeconds);
    }

    @Override
    public CompletableFuture<CloudQueueMessage> receiveAsync(String destination, int visibilityTimeoutInSeconds) {
        Assert.hasText(destination, "destination can't be null or empty");
        CompletableFuture<CloudQueueMessage> completableFuture = CompletableFuture.supplyAsync(() -> {
            CloudQueue cloudQueue = storageQueueClientFactory.getQueueCreator().apply(destination);
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
        return checkpointerGetter.apply(destination);
    }

    private Checkpointer<CloudQueueMessage> createCheckpointer(String destination) {
        return new StorageQueueCheckpointer(this.storageQueueClientFactory.getQueueCreator().apply(destination));
    }

    @Override
    public void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
    }

    @Override
    public int getVisibilityTimeoutInSeconds() {
        return this.visibilityTimeoutInSeconds;
    }
}
