/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.properties;

import com.microsoft.azure.spring.integration.core.api.CheckpointMode;

/**
 * @author Warren Zhu
 */
public class ServiceBusConsumerProperties {
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }
}
