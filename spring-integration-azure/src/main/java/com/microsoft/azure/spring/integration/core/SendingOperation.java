/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import java.util.concurrent.CompletableFuture;


/**
 * Operations for sending message to a destination.
 *
 * @param <D> the type of message
 * @author Warren Zhu
 */
public interface SendingOperation<D> {
    /**
     * Send a message to the given destination with a given partition supplier.
     */
    CompletableFuture<Void> sendAsync(String destination, D message, PartitionSupplier partitionSupplier);
}
