/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.config;

import com.microsoft.azure.spring.cloud.context.core.api.CredentialSupplier;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@ConfigurationProperties("spring.cloud.azure")
public class AzureProperties implements CredentialSupplier {

    @NotEmpty(message = "spring.cloud.azure.credential-file-path must be provided")
    private String credentialFilePath;

    @NotEmpty(message = "spring.cloud.azure.resource-group must be provided")
    private String resourceGroup;

    @NotEmpty(message = "spring.cloud.azure.region must be provided")
    private String region;

    private boolean autoCreateResources = false;
}
