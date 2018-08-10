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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StorageQueueMessageSource extends AbstractMessageSource<CloudQueueMessage> {

    private StorageQueueOperation storageQueueOperation;
    private String destination;
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;
    private Checkpointer<CloudQueueMessage> checkpointer;
    private int visibilityTimeoutInSeconds;
    private MessageConverter messageConverter;
    private Map<String, Object> commonHeaders = new HashMap<>();

    public StorageQueueMessageSource(String destination, StorageQueueOperation storageQueueOperation) {
        this.storageQueueOperation = storageQueueOperation;
        this.destination = destination;
        checkpointer = storageQueueOperation.getCheckpointer(destination);
        visibilityTimeoutInSeconds = storageQueueOperation.getVisibilityTimeoutInSeconds();
    }

    @Override
    protected Object doReceive() {
        CloudQueueMessage cloudQueueMessage;
        try {
            cloudQueueMessage = storageQueueOperation.receiveAsync(destination, visibilityTimeoutInSeconds).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new StorageQueueRuntimeException("Failed to receive message.", e);
        }
        if (cloudQueueMessage == null) {
            return null;
        } else {
            try {
                byte[] payload = cloudQueueMessage.getMessageContentAsByte();
                if (checkpointMode.equals(CheckpointMode.RECORD)) {
                    this.checkpointer.checkpoint(cloudQueueMessage);
                } else if (checkpointMode.equals(CheckpointMode.MANUAL)) {
                    this.commonHeaders.put(AzureHeaders.CHECKPOINTER, this.checkpointer);
                }
                return toMessage(payload);
            } catch (StorageException e) {
                throw new StorageQueueRuntimeException("Failed to get message content.", e);
            }
        }
    }

    private Message<?> toMessage(Object payload) {
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }
        return this.messageConverter.toMessage(payload, new MessageHeaders(commonHeaders));
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    public void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
        storageQueueOperation.setDefaultVisibilityTimeoutInSeconds(visibilityTimeoutInSeconds);
    }

    @Override
    public String getComponentType() {
        return "storage-queue:message-source";
    }



}
