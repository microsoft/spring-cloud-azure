/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import java.util.concurrent.CompletableFuture;

/**
 * Operations for adding or receiving message on destination queue.
 *
 * @param <T> the type of message
 * @author Miao Cao
 */
public interface QueueOperation<T> {

    /**
     * Adds a message to the back of destination queue.
     *
     * @param destination the destination queue name
     * @param t the message to add
     */
    CompletableFuture<Void> addAsync(String destination, T t);

    /**
     * Peeks a message from the front of destination queue.
     *
     * @param destination the destination queue name
     */
    CompletableFuture<T> peekAsync(String destination);

    /**
     * Get checkpointer for a given destination.
     * @param destination the destination queue name
     */
    Checkpointer<T> getCheckpointer(String destination);

}
