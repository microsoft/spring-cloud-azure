/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.keyvault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Azure Key Vault.
 * The real logic is in {@link com.microsoft.azure.spring.cloud.keyvault.KeyVaultEnvironmentPostProcessor}
 * This class is only for telemetry
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.keyVault.enabled", matchIfMissing = true)
@ConditionalOnClass(KeyVaultClient.class)
@EnableConfigurationProperties(AzureKeyVaultProperties.class)
public class AzureKeyVaultAutoConfiguration {
    private static final String KEY_VAULT = "KeyVault";

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, KEY_VAULT);
    }

}
