/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.storage.queue.converter.StorageQueueMessageConverter;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StorageQueueTemplate implements StorageQueueOperation {
    private static final int DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS = 30;
    private final StorageQueueClientFactory storageQueueClientFactory;
    private final String storageAccountName;

    @Getter
    @Setter
    protected StorageQueueMessageConverter messageConverter = new StorageQueueMessageConverter();

    @Getter
    @Setter
    private int visibilityTimeoutInSeconds = DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS;

    @Getter
    @Setter
    private Class<?> messagePayloadType = byte[].class;

    @Getter
    @Setter
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    public StorageQueueTemplate(@NonNull StorageQueueClientFactory storageQueueClientFactory,
            String storageAccountName) {
        this.storageQueueClientFactory = storageQueueClientFactory;
        this.storageAccountName = storageAccountName;
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String queueName, @NonNull Message<T> message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        CloudQueueMessage cloudQueueMessage = messageConverter.fromMessage(message, CloudQueueMessage.class);
        CloudQueue cloudQueue = storageQueueClientFactory.getOrCreateQueue(storageAccountName, queueName);
        return CompletableFuture.runAsync(() -> {
            try {
                cloudQueue.addMessage(cloudQueueMessage);
            } catch (StorageException e) {
                throw new StorageQueueRuntimeException("Failed to send message to storage queue", e);
            }
        });
    }

    @Override
    public CompletableFuture<Message<?>> receiveAsync(String queueName) {
        return this.receiveAsync(queueName, visibilityTimeoutInSeconds);
    }

    @Override
    public CompletableFuture<Message<?>> receiveAsync(String queueName, int visibilityTimeoutInSeconds) {
        Assert.hasText(queueName, "queueName can't be null or empty");

        return CompletableFuture.supplyAsync(() -> receiveMessage(queueName, visibilityTimeoutInSeconds));
    }

    private Message<?> receiveMessage(String queueName, int visibilityTimeoutInSeconds) {
        CloudQueue cloudQueue = storageQueueClientFactory.getOrCreateQueue(storageAccountName, queueName);
        CloudQueueMessage cloudQueueMessage;
        try {
            cloudQueueMessage = cloudQueue.retrieveMessage(visibilityTimeoutInSeconds, null, null);
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to receive message from storage queue", e);
        }

        Map<String, Object> headers = new HashMap<>();
        Checkpointer checkpointer = new AzureCheckpointer(() -> checkpoint(cloudQueue, cloudQueueMessage));

        if (checkpointMode == CheckpointMode.RECORD) {
            checkpointer.success();
        } else if (checkpointMode == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        if (cloudQueueMessage == null) {
            return null;
        }
        return messageConverter.toMessage(cloudQueueMessage, new MessageHeaders(headers), messagePayloadType);
    }

    private CompletableFuture<Void> checkpoint(CloudQueue cloudQueue, CloudQueueMessage cloudQueueMessage) {
        return CompletableFuture.runAsync(() -> {
            try {
                cloudQueue.deleteMessage(cloudQueueMessage);
            } catch (StorageException e) {
                throw new StorageQueueRuntimeException("Failed to checkpoint message from storage queue", e);
            }
        });
    }
}
