/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@AutoConfigureBefore(TelemetryAutoConfiguration.class)
@EnableConfigurationProperties(AzureCloudConfigProperties.class)
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {
    private static final String AZURE_CONFIG_STORE = "AzureConfigService";

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
        return new ConfigServiceTemplate(client, properties.getEndpoint(), properties.getCredential(),
                properties.getSecret());
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

