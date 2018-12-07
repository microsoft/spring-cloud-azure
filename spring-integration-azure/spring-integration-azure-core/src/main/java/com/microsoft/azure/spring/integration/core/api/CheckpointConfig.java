/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

/**
 * Checkpoint related config
 *
 * @author Warren Zhu
 */
public class CheckpointConfig {
    private final CheckpointMode checkpointMode;
    /**
     * The count of message to trigger checkpoint. Only used when {@link CheckpointMode#PARTITION_COUNT}
     */
    private final int checkpointCount;

    public CheckpointConfig(CheckpointMode checkpointMode, int checkpointCount) {
        this.checkpointMode = checkpointMode;
        this.checkpointCount = checkpointCount;
    }

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public int getCheckpointCount() {
        return checkpointCount;
    }

    @Override
    public String toString() {
        return "CheckpointConfig{" + "checkpointMode=" + checkpointMode + ", checkpointCount=" + checkpointCount + '}';
    }

    public static CheckpointConfigBuilder builder(){
        return new CheckpointConfigBuilder();
    }

    public static class CheckpointConfigBuilder {
        private CheckpointMode checkpointMode;
        private int checkpointCount;

        public CheckpointConfigBuilder checkpointMode(CheckpointMode checkpointMode) {
            this.checkpointMode = checkpointMode;
            return this;
        }

        public CheckpointConfigBuilder checkpointCount(int checkpointCount) {
            this.checkpointCount = checkpointCount;
            return this;
        }

        public CheckpointConfig build() {
            return new CheckpointConfig(checkpointMode, checkpointCount);
        }
    }

}
