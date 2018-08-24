/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.keyvault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.keyvault.KeyVaultOperation;
import com.microsoft.azure.spring.cloud.keyvault.KeyVaultTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Azure Key Vault.
 * The real logic is in {@link com.microsoft.azure.spring.cloud.keyvault.KeyVaultEnvironmentPostProcessor}
 * This class is only for telemetry
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureBefore(TelemetryAutoConfiguration.class)
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.keyvault.enabled", matchIfMissing = true)
@ConditionalOnClass({KeyVaultClient.class, KeyVaultOperation.class})
@EnableConfigurationProperties(AzureKeyVaultProperties.class)
public class AzureKeyVaultAutoConfiguration {
    private static final String KEY_VAULT = "KeyVault";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(KEY_VAULT);
    }

    @Bean
    @ConditionalOnMissingBean
    KeyVaultOperation keyVaultOperation(AzureKeyVaultProperties keyVaultProperties) {
        return new KeyVaultTemplate(keyVaultProperties.getClientId(), keyVaultProperties.getClientSecret());
    }

}
