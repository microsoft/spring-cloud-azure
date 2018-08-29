/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

import com.microsoft.azure.spring.integration.core.api.ListenerMode;

/**
 * The Checkpoint mode.
 *
 * @author Warren Zhu
 */
public enum CheckpointMode {

    /**
     * Checkpoint after each processed record.
     * Makes sense only if {@link ListenerMode#RECORD} is used.
     */
    RECORD,

    /**
     * Checkpoint after each processed batch of records.
     */
    BATCH,

    /**
     * User decide when to checkpoint manually
     */
    MANUAL,
}
