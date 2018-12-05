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
 * Properties for getting token from Azure Instance Metadata Service (IMDS) endpoint
 * <pre>
 * @see <a href="https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/how-to-use-vm-token#get-a-token-using-http">Get a token using HTTP<a/>
 * </pre>
 */
@Getter
@Setter
public class AzureCloudConfigMSIProperties {
    @Nullable
    private String objectId; // Optional: object_id of the managed identity

    @Nullable
    private String clientId; // Optional: client_id of the managed identity
}
