/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Provide partition id or key
 *
 * @author Warren Zhu
 */
@Getter
@Setter
public class PartitionSupplier {
    private String partitionKey;

    private String partitionId;
}
