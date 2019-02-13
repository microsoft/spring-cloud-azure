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
import com.microsoft.azure.spring.integration.eventhub.util.EventDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Warren Zhu
 */
public abstract class CheckpointManager {
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint %s on partition %s";
    private static final String CHECKPOINT_SUCCESS_MSG =
            "Consumer group '%s' checkpointed %s on partition %s in %s " + "mode";
    final CheckpointConfig checkpointConfig;

    public static CheckpointManager of(CheckpointConfig checkpointConfig) {
        switch (checkpointConfig.getCheckpointMode()) {
            case TIME:
                return new TimeCheckpointManager(checkpointConfig);
            case RECORD:
                return new RecordCheckpointManager(checkpointConfig);
            case BATCH:
                return new BatchCheckpointManager(checkpointConfig);
            case PARTITION_COUNT:
                return new PartitionCountCheckpointManager(checkpointConfig);
            case MANUAL:
                return new ManualCheckpointManager(checkpointConfig);
        }

        throw new IllegalArgumentException("Illegal checkpoint mode when building CheckpointManager");
    }

    CheckpointManager(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
    }

    public void onMessage(PartitionContext context, EventData eventData) {
        // no-op
    }

    public void completeBatch(PartitionContext context) {
        // no-op
    }

    void logCheckpointFail(PartitionContext context, EventData eventData, Throwable t) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(String
                    .format(CHECKPOINT_FAIL_MSG, context.getConsumerGroupName(), EventDataHelper.toString(eventData),
                            context.getPartitionId()), t);
        }
    }

    void logCheckpointSuccess(PartitionContext context, EventData eventData) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String
                    .format(CHECKPOINT_SUCCESS_MSG, context.getConsumerGroupName(), EventDataHelper.toString(eventData),
                            context.getPartitionId(), this.checkpointConfig.getCheckpointMode()));
        }
    }

    abstract Logger getLogger();
}
