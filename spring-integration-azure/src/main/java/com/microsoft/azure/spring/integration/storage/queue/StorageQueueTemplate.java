/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.*;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.storage.queue.converter.StorageQueueMessageConverter;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StorageQueueTemplate implements StorageQueueOperation {
    private final StorageQueueClientFactory storageQueueClientFactory;
    private static final int DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS = 30;

    @Getter
    @Setter
    private int visibilityTimeoutInSeconds = DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS;
    @Getter
    @Setter
    private Class messagePayloadType = byte[].class;
    @Getter
    @Setter
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    @Getter
    @Setter
    protected StorageQueueMessageConverter messageConverter = new StorageQueueMessageConverter();

    private Function<Pair<CloudQueue, CloudQueueMessage>, CompletableFuture<Void>> checkpoint = this::checkpointMessage;

    public StorageQueueTemplate(@NonNull StorageQueueClientFactory storageQueueClientFactory) {
        this.storageQueueClientFactory = storageQueueClientFactory;
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String destination, @NonNull Message<T> message,
                                             PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        CloudQueueMessage cloudQueueMessage = messageConverter.fromMessage(message, CloudQueueMessage.class);
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
    public CompletableFuture<Message<?>> receiveAsync(String destination) {
        return this.receiveAsync(destination, visibilityTimeoutInSeconds);
    }

    @Override
    public CompletableFuture<Message<?>> receiveAsync(String destination, int visibilityTimeoutInSeconds) {
        Assert.hasText(destination, "destination can't be null or empty");

        CompletableFuture<Message<?>> completableFuture = CompletableFuture.supplyAsync(
                () -> receiveMessage(destination, visibilityTimeoutInSeconds));
        return completableFuture;
    }

    private Message<?> receiveMessage(String destination, int visibilityTimeoutInSeconds) {
        CloudQueue cloudQueue = storageQueueClientFactory.getQueueCreator().apply(destination);
        CloudQueueMessage cloudQueueMessage;
        try {
            cloudQueueMessage = cloudQueue.retrieveMessage(visibilityTimeoutInSeconds, null, null);
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to peek message from cloud queue", e);
        }
        Map<String, Object> headers = new HashMap<>();
        Checkpointer checkpointer = new AzureCheckpointer(() ->
                checkpoint.apply(new Pair<>(cloudQueue, cloudQueueMessage)));

        if (checkpointMode == CheckpointMode.RECORD) {
            checkpointer.success();
        } else if (checkpointMode == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }
        Message<?> message = messageConverter.toMessage(cloudQueueMessage,
                new MessageHeaders(headers), messagePayloadType);
        return message;
    }

    private CompletableFuture<Void> checkpointMessage(Pair<CloudQueue, CloudQueueMessage> pair) {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            try {
                pair.getKey().deleteMessage(pair.getValue());
            } catch (StorageException e) {
                throw new StorageQueueRuntimeException("Failed to checkpoint message from cloud queue", e);
            }
        });
        return completableFuture;
    }
}
