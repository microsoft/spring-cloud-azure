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
import com.microsoft.azure.spring.integration.storage.queue.util.StorageQueueHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class StorageQueueTemplate implements StorageQueueOperation {
    private static final int DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS = 30;
    private static final String MSG_FAIL_CHECKPOINT = "Failed to checkpoint %s in storage queue '%s'";
    private static final String MSG_SUCCESS_CHECKPOINT = "Checkpointed %s in storage queue '%s' in %s mode";
    private final StorageQueueClientFactory storageQueueClientFactory;
    private final String storageAccountName;

    @Getter
    @Setter
    protected StorageQueueMessageConverter messageConverter = new StorageQueueMessageConverter();

    @Getter
    private int visibilityTimeoutInSeconds = DEFAULT_VISIBILITY_TIMEOUT_IN_SECONDS;

    @Getter
    private Class<?> messagePayloadType = byte[].class;

    @Getter
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    public StorageQueueTemplate(@NonNull StorageQueueClientFactory storageQueueClientFactory,
            String storageAccountName) {
        this.storageQueueClientFactory = storageQueueClientFactory;
        this.storageAccountName = storageAccountName;
        log.info("StorageQueueTemplate started with properties {}", buildProperties());
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String queueName, @NonNull Message<T> message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        CloudQueueMessage cloudQueueMessage = messageConverter.fromMessage(message, CloudQueueMessage.class);
        CloudQueue cloudQueue = storageQueueClientFactory.getOrCreateQueueClient(storageAccountName, queueName);
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
    public void setCheckpointMode(CheckpointMode checkpointMode) {
        Assert.state(isValidCheckpointMode(checkpointMode),
                "Only MANUAL or RECORD checkpoint mode is supported in StorageQueueTemplate");
        this.checkpointMode = checkpointMode;
        log.info("StorageQueueTemplate checkpoint mode becomes: {}", this.checkpointMode);
    }

    @Override
    public void setMessagePayloadType(Class<?> payloadType) {
        this.messagePayloadType = payloadType;
        log.info("StorageQueueTemplate messagePayloadType becomes: {}", this.messagePayloadType);
    }

    @Override
    public void setVisibilityTimeoutInSeconds(int timeout) {
        Assert.state(timeout > 0, "VisibilityTimeoutInSeconds should be positive");
        this.visibilityTimeoutInSeconds = timeout;
        log.info("StorageQueueTemplate VisibilityTimeoutInSeconds becomes: {}", this.visibilityTimeoutInSeconds);
    }

    private CompletableFuture<Message<?>> receiveAsync(String queueName, int visibilityTimeoutInSeconds) {
        Assert.hasText(queueName, "queueName can't be null or empty");

        return CompletableFuture.supplyAsync(() -> receiveMessage(queueName, visibilityTimeoutInSeconds));
    }

    private Message<?> receiveMessage(String queueName, int visibilityTimeoutInSeconds) {
        CloudQueue cloudQueue = storageQueueClientFactory.getOrCreateQueueClient(storageAccountName, queueName);
        CloudQueueMessage cloudQueueMessage;
        try {
            cloudQueueMessage = cloudQueue.retrieveMessage(visibilityTimeoutInSeconds, null, null);
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to receive message from storage queue", e);
        }

        Map<String, Object> headers = new HashMap<>();
        Checkpointer checkpointer = new AzureCheckpointer(() -> checkpoint(cloudQueue, cloudQueueMessage));

        if (checkpointMode == CheckpointMode.RECORD) {
            checkpointer.success().whenComplete((v, t) -> checkpointHandler(cloudQueueMessage, queueName, t));
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

    private Map<String, Object> buildProperties() {
        Map<String, Object> properties = new HashMap<>();

        properties.put("storageAccount", this.storageAccountName);
        properties.put("visibilityTimeout", this.visibilityTimeoutInSeconds);
        properties.put("messagePayloadType", this.messagePayloadType);
        properties.put("checkpointMode", this.checkpointMode);

        return properties;
    }

    private boolean isValidCheckpointMode(CheckpointMode checkpointMode) {
        return checkpointMode == CheckpointMode.MANUAL || checkpointMode == CheckpointMode.RECORD;
    }

    private void checkpointHandler(CloudQueueMessage message, String queueName, Throwable t) {
        if (t != null) {
            if (log.isWarnEnabled()) {
                log.warn(buildCheckpointFailMessage(message, queueName), t);
            }
        } else if (log.isDebugEnabled()) {
            log.debug(buildCheckpointSuccessMessage(message, queueName));
        }
    }

    private String buildCheckpointFailMessage(CloudQueueMessage cloudQueueMessage, String queueName) {
        return String.format(MSG_FAIL_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName);
    }

    private String buildCheckpointSuccessMessage(CloudQueueMessage cloudQueueMessage, String queueName) {
        return String.format(MSG_SUCCESS_CHECKPOINT, StorageQueueHelper.toString(cloudQueueMessage), queueName,
                checkpointMode);
    }
}
