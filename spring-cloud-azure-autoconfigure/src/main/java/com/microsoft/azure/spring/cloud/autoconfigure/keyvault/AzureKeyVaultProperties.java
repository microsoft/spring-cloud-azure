/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.keyvault;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@Getter
@Setter
@ConfigurationProperties("spring.cloud.azure.keyvault")
public class AzureKeyVaultProperties {
    private String name;
    private String clientId;
    private String clientKey;

    @PostConstruct
    public void validate() {
        Assert.hasText(name, "spring.cloud.azure.keyvault.name must be provided.");
        Assert.hasText(clientId, "spring.cloud.azure.keyvault.client-id must be provided.");
        Assert.hasText(clientId, "spring.cloud.azure.keyvault.client-key must be provided.");
    }
}
