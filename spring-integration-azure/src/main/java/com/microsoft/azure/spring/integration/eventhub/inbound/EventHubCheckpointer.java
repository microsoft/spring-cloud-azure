/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.Checkpointer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EventHubCheckpointer implements Checkpointer<EventData> {
    private final Map<String, PartitionContext> partitionContextMap = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> checkpoint() {
        return CompletableFuture.allOf(partitionContextMap.values().stream().map(PartitionContext::checkpoint)
                                                          .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> checkpoint(EventData eventData) {
        // event hub event processor host unsupported
        return null;
    }

    public void addPartitionContext(PartitionContext partitionContext) {
        partitionContextMap.putIfAbsent(partitionContext.getPartitionId(), partitionContext);
    }

    public void removePartitionContext(PartitionContext partitionContext) {
        partitionContextMap.remove(partitionContext.getPartitionId());
    }
}
