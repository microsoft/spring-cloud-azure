/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud Bootstrap Configuration for setting up an {@link KeyVaultPropertySourceLocator}.
 */
@Configuration
@EnableConfigurationProperties(KeyVaultConfigProperties.class)
@ConditionalOnProperty(name = KeyVaultConfigProperties.ENABLED, matchIfMissing = true)
public class KeyVaultConfigBootstrapConfiguration {

    @Bean
    KeyVaultPropertySourceLocator keyVaultPropertySourceLocator(KeyVaultConfigProperties properties) {
        return new KeyVaultPropertySourceLocator(properties);
    }
}
