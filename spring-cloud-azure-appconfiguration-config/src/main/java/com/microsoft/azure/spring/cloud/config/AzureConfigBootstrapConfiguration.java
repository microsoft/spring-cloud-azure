/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.context.core.config.AzureManagedIdentityProperties;

@Configuration
@EnableConfigurationProperties({ AzureCloudConfigProperties.class, AppConfigProviderProperties.class })
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {

    private static final String ENV_MSI_ENDPOINT = "MSI_ENDPOINT";

    private static final String ENV_MSI_SECRET = "MSI_SECRET";

    private static final String TELEMETRY_SERVICE = "AppConfiguration";

    @Bean
    public AzureTokenCredentials tokenCredentials(AzureCloudConfigProperties properties) {
        if (StringUtils.hasText(System.getenv(ENV_MSI_ENDPOINT))
                && StringUtils.hasText(System.getenv(ENV_MSI_SECRET))) {
            return new AppServiceMSICredentials(AzureEnvironment.AZURE);
        }

        AzureManagedIdentityProperties msiProps = properties.getManagedIdentity();
        MSICredentials credentials = new MSICredentials();
        if (msiProps != null && msiProps.getClientId() != null) {
            credentials.withClientId(msiProps.getClientId());
        } else if (msiProps != null && msiProps.getObjectId() != null) {
            credentials.withObjectId(msiProps.getObjectId());
        }

        return credentials;
    }

    @Bean
    public AzureConfigPropertySourceLocator sourceLocator(
            AzureCloudConfigProperties properties, PropertyCache propertyCache,
            ClientStore clients) {
        return new AzureConfigPropertySourceLocator(properties, propertyCache, clients);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyCache getPropertyCache() {
        return PropertyCache.getPropertyCache();
    }

    @Bean
    public ClientStore buildClientStores(AzureCloudConfigProperties properties) {
        return new ClientStore(properties);
    }

    @Bean
    public AzureConfigPropertySourceLocator sourceLocator(ConfigServiceOperations operations,
            AzureCloudConfigProperties properties, PropertyCache propertyCache, 
            AppConfigProviderProperties appProperties) {
        return new AzureConfigPropertySourceLocator(operations, properties, propertyCache, appProperties);
    }
    
    @Bean
    public PropertyCache getPropertyCache() {
        return PropertyCache.getPropertyCache();
    }

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(TELEMETRY_SERVICE);
    }
}
