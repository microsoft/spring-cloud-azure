/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import org.springframework.messaging.Message;
import java.util.concurrent.CompletableFuture;

/**
 * Azure storage queue operation to support send and receive
 * {@link org.springframework.messaging.Message} asynchronously
 * @author Miao Cao
 */
public interface StorageQueueOperation extends SendOperation {

    /**
     * Receives a message from the front of destination queue.
     * This operation marks the retrieved message as invisible in the queue for a visibility timeout period.
     * The default visibility timeout is 30 seconds. You can change visibility timeout by
     * {@link #setVisibilityTimeoutInSeconds(int) }
     * You should check point if message has been processed successfully, otherwise the message will be visible
     * in the queue again.
     *
     * @param destination the destination queue name
     */
    CompletableFuture<Message<?>> receiveAsync(String destination);

    /**
     * Receives a message from the front of destination queue.
     * This operation marks the retrieved message as invisible in the queue for a visibility timeout period.
     * The default visibility timeout is 30 seconds.
     * You should check point if message has been processed successfully, otherwise the message will be visible
     * in the queue again.
     *
     * @param destination the destination queue name
     * @param visibilityTimeoutInSeconds Specifies the visibility timeout for the message, in seconds
     */
    CompletableFuture<Message<?>> receiveAsync(String destination, int visibilityTimeoutInSeconds);

    /**
     * Set visibility timeout.
     * @param visibilityTimeoutInSeconds Specifies the visibility timeout for the message, in seconds
     */
    void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds);

    int getVisibilityTimeoutInSeconds();

    /**
     * Set visibility timeout.
     * @param checkpointMode Specifies checkpoint mode, default checkpoint mode is RECORD
     */
    void setCheckpointMode(CheckpointMode checkpointMode);

    CheckpointMode getCheckpointMode();

    /**
     * Set visibility timeout.
     * @param messagePayloadType Specifies the payload type of the message, the default payload type is byte[]
     */
    void setMessagePayloadType(Class messagePayloadType);

    Class getMessagePayloadType();
}
