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
 *
 * @author Miao Cao
 */
public interface StorageQueueOperation extends SendOperation {

    /**
     * Receives a message from the head of queue. This message become invisible for a visibility timeout period.
     * You can change visibility timeout by {@link #setVisibilityTimeoutInSeconds(int)}
     * You should checkpoint if message has been processed successfully, otherwise it will be visible again.
     */
    CompletableFuture<Message<?>> receiveAsync(String queueName);

    /**
     * Receives a message from the head of queue. This message become invisible for a visibility timeout period.
     * You should check point if message has been processed successfully, otherwise it will be visible again.
     */
    CompletableFuture<Message<?>> receiveAsync(String queueName, int visibilityTimeoutInSeconds);

    int getVisibilityTimeoutInSeconds();

    /**
     * Set visibility timeout. Default is 30
     *
     */
    void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds);

    CheckpointMode getCheckpointMode();

    /**
     * Set checkpoint mode. Default is {@link CheckpointMode#RECORD}
     *
     */
    void setCheckpointMode(CheckpointMode checkpointMode);

    Class<?> getMessagePayloadType();

    /**
     * Set payload type. Default is {@link byte[]}
     *
     * @param messagePayloadType Specifies the payload type of {@link Message}
     */
    void setMessagePayloadType(Class<?> messagePayloadType);
}
