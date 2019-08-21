/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "watch.enabled")
    static class CloudWatchAutoConfiguration {
        @Bean
        public AzureCloudConfigWatch getConfigWatch(
                AzureCloudConfigProperties properties,
                AzureConfigPropertySourceLocator sourceLocator, PropertyCache propertyCache,
                ClientStore clientStore) {
            return new AzureCloudConfigWatch(properties, sourceLocator.getStoreContextsMap(), propertyCache,
                    clientStore);
        }

        @Bean
        @ConditionalOnMissingBean
        public PropertyCache getPropertyCache() {
            return new PropertyCache();
        }

        @Bean
        public ConfigListener configListener(AzureCloudConfigWatch azureCloudConfigWatch) {
            return new ConfigListener(azureCloudConfigWatch);
        }
    }
}
