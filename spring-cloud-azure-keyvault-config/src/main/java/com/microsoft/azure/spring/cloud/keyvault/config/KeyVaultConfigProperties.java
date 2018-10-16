/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the Azure Key Vault integration with Spring Cloud Config.
 */
@ConfigurationProperties(KeyVaultConfigProperties.CONFIG_PREFIX)
@Validated
public class KeyVaultConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.keyvault.config";
    public static final String ENABLED = CONFIG_PREFIX + ".enabled";

    @Getter @Setter
    private boolean enabled = true;

    @Getter @Setter
    private boolean failFast = true;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String activeProfile;
}
