/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle real checkpoint based on {@link CheckpointConfig}
 *
 * @author Warren Zhu
 */

@Slf4j
@NotThreadSafe
class EventHubCheckpointManager {
    private final CheckpointConfig checkpointConfig;
    private final ConcurrentHashMap<String, AtomicInteger> countByPartition = new ConcurrentHashMap<>();

    EventHubCheckpointManager(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
    }

    void onMessage(PartitionContext context, EventData eventData) {
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
            context.checkpoint(eventData).whenComplete(this::checkpointHandler);
        } else if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.PARTITION_COUNT) {
            String partitionId = context.getPartitionId();
            this.countByPartition.putIfAbsent(partitionId, new AtomicInteger(1));
            AtomicInteger count = this.countByPartition.get(partitionId);
            if (count.incrementAndGet() >= checkpointConfig.getCheckpointCount()) {
                context.checkpoint(eventData).whenComplete(this::checkpointHandler);
                count.set(0);
            }
        }
    }

    void completeBatch(PartitionContext context) {
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
            context.checkpoint().whenComplete(this::checkpointHandler);
        }
    }

    private void checkpointHandler(Void v, Throwable t) {
        if (t != null) {
            log.warn("Failed to checkpoint", t);
        }
    }
}
