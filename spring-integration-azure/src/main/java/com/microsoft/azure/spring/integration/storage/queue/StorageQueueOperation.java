/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Operations for sending or receiving message on destination Azure storage queue.
 *
 * @author Miao Cao
 */
public interface StorageQueueOperation extends SendOperation<CloudQueueMessage> {

    /**
     * Receives a message from the front of destination queue.
     * This operation marks the retrieved message as invisible in the queue for a visibility timeout period.
     * The default visibility timeout is 30 seconds.
     * You should check point if message has been processed successfully, otherwise the message will be visible
     * in the queue again.
     *
     * @param destination the destination queue name
     */
    CompletableFuture<CloudQueueMessage> receiveAsync(String destination);

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
    CompletableFuture<CloudQueueMessage> receiveAsync(String destination, int visibilityTimeoutInSeconds);

    /**
     * Get checkpointer for a given destination.
     * @param destination the destination queue name
     */
    Checkpointer<CloudQueueMessage> getCheckpointer(String destination);

    /**
     * Set visibility timeout.
     * @param visibilityTimeoutInSeconds Specifies the visibility timeout for the message, in seconds
     */
    void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds);

    int getVisibilityTimeoutInSeconds();
}
