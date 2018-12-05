/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.config;

import com.microsoft.azure.spring.cloud.context.core.api.CredentialSupplier;
import com.microsoft.azure.spring.cloud.context.core.api.Environment;
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

    private String credentialFilePath;

    private String resourceGroup;

    private Environment environment = Environment.GLOBAL;

    private String region;

    private boolean autoCreateResources = false;

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                    "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }
    }
}
