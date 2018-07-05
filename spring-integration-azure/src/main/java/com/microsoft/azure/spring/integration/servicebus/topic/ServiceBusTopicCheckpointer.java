/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.ISubscriptionClient;
import com.microsoft.azure.spring.integration.core.Checkpointer;

import java.util.concurrent.CompletableFuture;

public class ServiceBusTopicCheckpointer implements Checkpointer<IMessage> {
    private final ISubscriptionClient subscriptionClient;

    public ServiceBusTopicCheckpointer(ISubscriptionClient subscriptionClient) {
        this.subscriptionClient = subscriptionClient;
    }

    @Override
    public CompletableFuture<Void> checkpoint() {
        // Service bus unsupported
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> checkpoint(IMessage iMessage) {
        return this.subscriptionClient.completeAsync(iMessage.getLockToken());
    }
}
