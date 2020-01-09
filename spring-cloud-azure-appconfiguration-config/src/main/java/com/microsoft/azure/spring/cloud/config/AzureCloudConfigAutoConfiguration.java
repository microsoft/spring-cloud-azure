/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.spring.cloud.config.stores.ClientStore;

@Configuration
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureCloudConfigAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RefreshEndpoint.class)
    static class CloudWatchAutoConfiguration {

        @Bean
        public AzureCloudConfigRefresh getConfigWatch(AzureCloudConfigProperties properties,
                AzureConfigPropertySourceLocator sourceLocator, ClientStore clientStore) {
            return new AzureCloudConfigRefresh(properties, sourceLocator.getStoreContextsMap(), clientStore);
        }

        @Bean
        public ConfigListener configListener(AzureCloudConfigRefresh azureCloudConfigWatch) {
            return new ConfigListener(azureCloudConfigWatch);
        }
    }
}
