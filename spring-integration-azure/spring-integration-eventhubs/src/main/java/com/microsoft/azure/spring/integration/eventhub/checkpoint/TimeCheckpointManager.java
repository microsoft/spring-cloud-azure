/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.checkpoint;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Do checkpoint when the time since last successful checkpoint exceeds {@link CheckpointConfig#getCheckpointInterval()}
 * for one partition. Effective when {@link CheckpointMode#PARTITION_COUNT}
 *
 * @author Warren Zhu
 */
class TimeCheckpointManager extends CheckpointManager {
    private static final Logger log = LoggerFactory.getLogger(TimeCheckpointManager.class);
    private final AtomicReference<LocalDateTime> lastCheckpointTime = new AtomicReference<>(LocalDateTime.now());

    TimeCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getCheckpointMode() == CheckpointMode.TIME,
                () -> "TimeCheckpointManager should have checkpointMode time");
    }

    public void onMessage(PartitionContext context, EventData eventData) {
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(now, this.lastCheckpointTime.get())
                    .compareTo(this.checkpointConfig.getCheckpointInterval()) > 0) {
            context.checkpoint(eventData).whenComplete((v, t) -> {
                if (t != null) {
                    logCheckpointFail(context, eventData, t);
                } else {
                    logCheckpointSuccess(context, eventData);
                    lastCheckpointTime.set(now);
                }
            });
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
