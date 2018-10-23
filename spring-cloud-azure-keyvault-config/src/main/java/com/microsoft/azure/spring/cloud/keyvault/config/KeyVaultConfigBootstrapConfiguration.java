/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.*;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Spring Cloud Bootstrap Configuration for setting up an {@link KeyVaultPropertySourceLocator}.
 */
@Configuration
@AutoConfigureBefore(TelemetryAutoConfiguration.class)
@ConditionalOnProperty(name = KeyVaultConfigProperties.ENABLED, matchIfMissing = true)
@EnableConfigurationProperties(KeyVaultConfigProperties.class)
public class KeyVaultConfigBootstrapConfiguration {
    private static final String KEY_VAULT_CONFIG = "KeyVaultConfig";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(KEY_VAULT_CONFIG);
    }

    @Bean
    public KeyVaultClient keyVaultClient(KeyVaultConfigProperties properties, AuthenticationExecutorFactory factory) {
        final Credentials credentials = properties.getCredentials();
        AuthenticationExecutor executor = factory.create(credentials);
        return new KeyVaultClient(new AadKeyVaultCredentials(executor));
    }

    @Bean
    public KeyVaultPropertySourceLocator keyVaultPropertySourceLocator(KeyVaultClient client,
                                                                       KeyVaultConfigProperties properties) {
        return new KeyVaultPropertySourceLocator(client, properties);
    }

    @Bean
    public AuthenticationExecutorFactory authenticationExecutorFactory() {
        return new DefaultAuthenticationExecutorFactory();
    }
}
