/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.inbound;

import java.util.concurrent.CompletableFuture;

/**
 * A callback to perform checkpoint.
 *
 * @param <T> message type parameter
 * @author Warren Zhu
 */
public interface Checkpointer<T> {

    /**
     * Checkpoint to last record processed by {@link Subscriber}. Please check result to detect failure
     */
    CompletableFuture<Void> checkpoint();

    /**
     * Checkpoint to provided generic record. Please check result to detect failure
     */
    CompletableFuture<Void> checkpoint(T t);
}
