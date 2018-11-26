/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.impl;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.eventhub.util.EventDataHelper;
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
    private final ConcurrentHashMap<String, EventData> lastEventByPartition = new ConcurrentHashMap<>();

    EventHubCheckpointManager(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
    }

    protected void onMessage(PartitionContext context, EventData eventData) {
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD) {
            context.checkpoint(eventData).whenComplete((v, t) -> checkpointHandler(context, eventData, t));
        } else if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.PARTITION_COUNT) {
            String partitionId = context.getPartitionId();
            this.countByPartition.computeIfAbsent(partitionId, (k) -> new AtomicInteger(0));
            AtomicInteger count = this.countByPartition.get(partitionId);
            if (count.incrementAndGet() >= checkpointConfig.getCheckpointCount()) {
                context.checkpoint(eventData).whenComplete((v, t) -> checkpointHandler(context, eventData, t));
                count.set(0);
            }
        }

        this.lastEventByPartition.put(context.getPartitionId(), eventData);
    }

    protected void completeBatch(PartitionContext context) {
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
            EventData eventData = this.lastEventByPartition.get(context.getPartitionId());
            context.checkpoint().whenComplete((v, t) -> checkpointHandler(context, eventData, t));
        }
    }

    private String buildCheckpointFailMessage(PartitionContext context, EventData eventData) {
        String message = "Consumer group '%s' failed to checkpoint %s on partition %s";
        return String.format(message, context.getConsumerGroupName(), EventDataHelper.toString(eventData),
                context.getPartitionId());
    }

    private String buildCheckpointSuccessMessage(PartitionContext context, EventData eventData) {
        String message = "Consumer group '%s' checkpointed %s on partition %s in %s mode";
        return String.format(message, context.getConsumerGroupName(), EventDataHelper.toString(eventData),
                context.getPartitionId(), this.checkpointConfig.getCheckpointMode());
    }

    private void checkpointHandler(PartitionContext context, EventData eventData, Throwable t) {
        if (t != null) {
            if (log.isWarnEnabled()) {
                log.warn(buildCheckpointFailMessage(context, eventData), t);
            }
        } else if (log.isDebugEnabled()) {
            log.debug(buildCheckpointSuccessMessage(context, eventData));
        }
    }
}
