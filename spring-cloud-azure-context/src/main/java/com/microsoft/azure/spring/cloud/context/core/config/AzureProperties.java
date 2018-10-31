/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.config;

import com.microsoft.azure.spring.cloud.context.core.api.CredentialSupplier;
import com.microsoft.azure.spring.cloud.context.core.api.Region;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@ConfigurationProperties("spring.cloud.azure")
public class AzureProperties implements CredentialSupplier {

    @NotEmpty
    private String credentialFilePath;

    @NotEmpty
    private String resourceGroup;

    private Region region = Region.US;

    private String location;

    private boolean autoCreateResources = false;

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.location,
                    "When auto create resources is enabled, spring.cloud.azure.location must be provided");
        }
    }
}
