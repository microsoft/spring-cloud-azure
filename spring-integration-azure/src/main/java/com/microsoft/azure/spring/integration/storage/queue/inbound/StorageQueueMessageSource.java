/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.inbound;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueRuntimeException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Inbound Message Source to receive messages from Azure Storage Queue.
 *
 * @author Miao Cao
 */
public class StorageQueueMessageSource extends AbstractMessageSource<CloudQueueMessage> {

    private StorageQueueOperation storageQueueOperation;
    private String destination;
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;
    private Checkpointer<CloudQueueMessage> checkpointer;
    private Map<String, Object> commonHeaders = new HashMap<>();

    public StorageQueueMessageSource(String destination, StorageQueueOperation storageQueueOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.storageQueueOperation = storageQueueOperation;
        this.destination = destination;
        this.checkpointer = storageQueueOperation.getCheckpointer(destination);
    }

    @Override
    protected Object doReceive() {
        CloudQueueMessage cloudQueueMessage;
        try {
            cloudQueueMessage = storageQueueOperation.receiveAsync(destination).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new StorageQueueRuntimeException("Failed to receive message.", e);
        }
        if (cloudQueueMessage == null) {
            return null;
        }
        byte[] payload;
        try {
            payload = cloudQueueMessage.getMessageContentAsByte();
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to get message content.", e);
        }
        if (checkpointMode.equals(CheckpointMode.RECORD)) {
                checkpointer.checkpoint(cloudQueueMessage);
        }
        return toMessage(payload);
    }

    private Message<?> toMessage(Object payload) {
        return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
        if (checkpointMode.equals(CheckpointMode.MANUAL) && commonHeaders.size() == 0) {
            commonHeaders.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }
    }

    @Override
    public String getComponentType() {
        return "storage-queue:message-source";
    }
}
