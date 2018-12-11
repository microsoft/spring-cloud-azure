/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Properties for acquiring connection string from Azure Resource Management.
 */
@Getter
@Setter
public class AzureCloudConfigARMProperties {
    @Nullable
    private String subscriptionId;

    @Nullable
    private String resourceGroup;

    @Nullable
    private String configStore;
}
