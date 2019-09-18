/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.config.managed.identity.AzureResourceManagerConnector;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import com.microsoft.azure.spring.cloud.context.core.config.AzureManagedIdentityProperties;

@Configuration
@EnableConfigurationProperties({ AzureCloudConfigProperties.class, AppConfigProviderProperties.class })
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigBootstrapConfiguration.class);

    private static final String ENV_MSI_ENDPOINT = "MSI_ENDPOINT";

    private static final String ENV_MSI_SECRET = "MSI_SECRET";

    private static final String TELEMETRY_SERVICE = "AppConfiguration";

    private static final String TELEMETRY_KEY = "HashedStoreName";

    @Bean
    public ConnectionStringPool initConnectionString(AzureCloudConfigProperties properties,
            AzureTokenCredentials credentials) {
        ConnectionStringPool pool = new ConnectionStringPool();
        List<ConfigStore> stores = properties.getStores();

        for (ConfigStore store : stores) {
            if (StringUtils.hasText(store.getName()) && StringUtils.hasText(store.getConnectionString())) {
                pool.put(store.getName(), ConnectionString.of(store.getConnectionString()));
            } else if (StringUtils.hasText(store.getName())) {
                // Try load connection string from ARM if connection string is not
                // configured
                LOGGER.info("Load connection string for store [{}] from Azure Resource Management, " +
                        "Azure managed identity should be enabled.", store.getName());
                AzureResourceManagerConnector armConnector = new AzureResourceManagerConnector(credentials,
                        store.getName());

                String connectionString = armConnector.getConnectionString();
                Assert.hasText(connectionString, "Connection string cannot be empty");

                pool.put(store.getName(), ConnectionString.of(connectionString));
            }

            TelemetryCollector.getInstance().addProperty(TELEMETRY_SERVICE, TELEMETRY_KEY, sha256Hex(store.getName()));
        }

        Assert.notEmpty(pool.getAll(), "Connection string pool for the configuration stores is empty");

        return pool;
    }

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
    public CloseableHttpClient closeableHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public ConfigHttpClient httpClient(CloseableHttpClient httpClient) {
        return new ConfigHttpClient(httpClient);
    }

    @Bean
    public ConfigServiceOperations azureConfigOperations(ConfigHttpClient client, ConnectionStringPool pool,
            AppConfigProviderProperties properties) {
        return new ConfigServiceTemplate(client, pool, properties);
    }

    @Bean
    public AzureConfigPropertySourceLocator sourceLocator(ConfigServiceOperations operations,
            AzureCloudConfigProperties properties, PropertyCache propertyCache, AppConfigProviderProperties appProperties) {
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
