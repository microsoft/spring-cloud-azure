/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.config.msi.AzureConfigMSIConnector;
import com.microsoft.azure.spring.cloud.config.msi.ConfigAccessKeyResource;
import com.microsoft.azure.spring.cloud.config.msi.ConfigMSICredentials;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(AzureCloudConfigProperties.class)
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {
    private static final String AZURE_CONFIG_STORE = "AzureConfigService";

    @Bean
    @Primary
    public AzureCloudConfigProperties initProperties(AzureCloudConfigProperties properties) {
        if (StringUtils.hasText(properties.getConnectionString())) {
            return properties;
        }

        ConfigMSICredentials msiCredentials = new ConfigMSICredentials(properties.getMsi());
        ConfigAccessKeyResource keyResource = new ConfigAccessKeyResource(properties.getArm());

        AzureConfigMSIConnector msiConnector = new AzureConfigMSIConnector(msiCredentials, keyResource);
        properties.setConnectionString(msiConnector.getConnectionString());

        Assert.hasText(properties.getConnectionString(), "Connection string cannot be empty");

        return properties;
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
    public ConfigServiceOperations azureConfigOperations(ConfigHttpClient client,
                                                         AzureCloudConfigProperties properties) {
        return new ConfigServiceTemplate(client, properties.getEndpoint(), properties.getId(), properties.getSecret());
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
