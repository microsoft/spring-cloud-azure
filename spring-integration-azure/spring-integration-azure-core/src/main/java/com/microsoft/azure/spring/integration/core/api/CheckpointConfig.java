/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Checkpoint related config
 *
 * @author Warren Zhu
 */
@Getter
@Builder
@ToString
public class CheckpointConfig {
    private final CheckpointMode checkpointMode;
    /**
     * The count of message to trigger checkpoint. Only used when {@link CheckpointMode#PARTITION_COUNT}
     */
    private final int checkpointCount;
}
