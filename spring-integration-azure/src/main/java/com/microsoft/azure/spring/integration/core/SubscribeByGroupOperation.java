/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import java.util.function.Consumer;

/**
 * Operations for subscribing to a destination with a consumer group.
 *
 * @author Warren Zhu
 */
public interface SubscribeByGroupOperation<D, K> {

    /**
     * Register a message consumer to a given destination with a given consumer group.
     *
     * @return {@code true} if the consumer was subscribed or {@code false} if it
     * was already subscribed.
     */
    boolean subscribe(String destination, Consumer<Iterable<D>> consumer, String consumerGroup);

    /**
     * Un-register a message consumer with a given destination and consumer group.
     *
     * @return {@code true} if the consumer was un-registered, or {@code false}
     * if was not registered.
     */
    boolean unsubscribe(String destination, Consumer<Iterable<D>> consumer, String consumerGroup);

    /**
     * Get checkpointer for a given destination and consumer group
     */
    Checkpointer<K> getCheckpointer(String destination, String consumerGroup);
}
