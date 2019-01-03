/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.config.msi.AzureCloudConfigMSIProperties;
import com.microsoft.azure.spring.cloud.config.msi.AzureConfigMSIConnector;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@EnableConfigurationProperties(AzureCloudConfigProperties.class)
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {
    private static final String AZURE_CONFIG_STORE = "AzureConfigService";
    private static final String ENV_MSI_ENDPOINT = "MSI_ENDPOINT";
    private static final String ENV_MSI_SECRET = "MSI_SECRET";

    @Bean
    public ConnectionStringPool initConnectionString(AzureCloudConfigProperties properties,
                                                     AzureTokenCredentials credentials) {
        ConnectionStringPool pool = new ConnectionStringPool();
        List<ConfigStore> stores = properties.getStores();

        if (!properties.isMsiEnabled()) {
            // Initialize connection string pool directly if MSI not enabled.
            stores.forEach(store -> pool.put(store.getName(), ConnectionString.of(store.getConnectionString())));
        } else {
            // Try load connection string from ARM if MSI enabled
            stores.stream().forEach(store -> {
                AzureConfigMSIConnector msiConnector = new AzureConfigMSIConnector(credentials, store.getName());

                String connectionString = msiConnector.getConnectionString();
                Assert.hasText(connectionString, "Connection string cannot be empty");

                pool.put(store.getName(), ConnectionString.of(connectionString));
            });
        }

        return pool;
    }

    @Bean
    public AzureTokenCredentials tokenCredentials(AzureCloudConfigProperties properties) {
        if (StringUtils.hasText(System.getenv(ENV_MSI_ENDPOINT))
                && StringUtils.hasText(System.getenv(ENV_MSI_SECRET))) {
            return new AppServiceMSICredentials(AzureEnvironment.AZURE);
        }

        AzureCloudConfigMSIProperties msiProps = properties.getMsi();
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
    public ConfigServiceOperations azureConfigOperations(ConfigHttpClient client, ConnectionStringPool pool) {
        return new ConfigServiceTemplate(client, pool);
    }

    @Bean
    public AzureConfigPropertySourceLocator sourceLocator(ConfigServiceOperations operations,
                                                          AzureCloudConfigProperties properties) {
        return new AzureConfigPropertySourceLocator(operations, properties);
    }

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(AZURE_CONFIG_STORE);
    }
}
