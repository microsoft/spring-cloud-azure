/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

/**
 * Operations for queue service.
 *
 * @author Miao Cao
 */
public interface QueueOperation<T> {

    /**
     * Adds a message to the back of destination queue.
     *
     * @return {@code true} if this queue changed as a result of the call
     */
    boolean add(String destination, T t);

    /**
     * Peeks a message from the front of destination queue.
     *
     */
    T peek(String destination);

    /**
     * Retrieves a message from the front of destination queue.
     *
     */
    T retrieve(String destination);

    /**
     * Deletes the specified message from destination queue.
     *
     * @return {@code true} if this queue changed as a result of the call
     */
    boolean delete(String destination, T t);
}
