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

/**
 * Do checkpoint after each message successfully processed.
 * Effective when {@link CheckpointMode#RECORD}
 *
 * @author Warren Zhu
 */
class RecordCheckpointManager extends CheckpointManager {
    private static final Logger log = LoggerFactory.getLogger(RecordCheckpointManager.class);

    RecordCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD,
                () -> "RecordCheckpointManager should have checkpointMode record");
    }

    public void onMessage(PartitionContext context, EventData eventData) {
        context.checkpoint(eventData).whenComplete((v, t) -> {
            if (t != null) {
                logCheckpointFail(context, eventData, t);
            } else {
                logCheckpointSuccess(context, eventData);
            }
        });
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
