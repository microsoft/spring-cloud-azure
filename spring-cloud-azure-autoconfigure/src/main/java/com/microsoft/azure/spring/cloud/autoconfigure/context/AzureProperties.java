/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.spring.cloud.context.core.CredentialSupplier;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

@Getter
@Setter
@ConfigurationProperties("spring.cloud.azure")
public class AzureProperties implements CredentialSupplier {

    private String credentialFilePath;

    private String resourceGroup;

    private String region;

    @PostConstruct
    public void validate() {
        Assert.hasText(credentialFilePath, "spring.cloud.azure.credentialFilePath must be provided");
        Assert.hasText(resourceGroup, "spring.cloud.azure.resourceGroup must be provided");
        Assert.hasText(region, "spring.cloud.azure.region must be provided");
    }
}
