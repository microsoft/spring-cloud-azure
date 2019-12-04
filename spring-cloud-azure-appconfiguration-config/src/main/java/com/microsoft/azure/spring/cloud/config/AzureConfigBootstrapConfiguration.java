/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;
import com.microsoft.azure.spring.cloud.context.core.config.AzureManagedIdentityProperties;

@Configuration
@EnableConfigurationProperties({ AzureCloudConfigProperties.class, AppConfigProviderProperties.class })
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigBootstrapConfiguration.class);

    @Bean
    public ConnectionPool initConnectionString(AzureCloudConfigProperties properties) {
        ConnectionPool pool = new ConnectionPool();
        List<ConfigStore> stores = properties.getStores();

        for (ConfigStore store : stores) {
            if (StringUtils.hasText(store.getName()) && StringUtils.hasText(store.getConnectionString())) {
                pool.put(store.getName(), new Connection(store.getConnectionString()));
            } else if (StringUtils.hasText(store.getName())) {
                AzureManagedIdentityProperties msiProps = properties.getManagedIdentity();

                String endpoint = "https://" + store.getName() + ".azconfig.io";
                if (msiProps != null && msiProps.getClientId() != null) {
                    pool.put(store.getName(), new Connection(endpoint, msiProps.getClientId()));
                } else {
                    pool.put(store.getName(), new Connection(endpoint, ""));
                }

            }
        }

        Assert.notEmpty(pool.getAll(), "Connection string pool for the configuration stores is empty");

        return pool;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public AzureConfigPropertySourceLocator sourceLocator(AzureCloudConfigProperties properties,
            AppConfigProviderProperties appProperties, ClientStore clients, ApplicationContext context) {
        TokenCredentialProvider tokenCredentialProvider = null;
        try {
            tokenCredentialProvider = context.getBean(TokenCredentialProvider.class);
        } catch (NoUniqueBeanDefinitionException e) {
            LOGGER.error("Failed to find unique TokenCredentialProvider Bean for authentication.", e);
            if (properties.isFailFast()) {
                throw e;
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("No TokenCredentialProvider found. " + e.getMessage());
        }
        return new AzureConfigPropertySourceLocator(properties, appProperties, clients, tokenCredentialProvider);
    }

    @Bean
    public ClientStore buildClientStores(AzureCloudConfigProperties properties,
            AppConfigProviderProperties appProperties, ConnectionPool pool, ApplicationContext context) {
        TokenCredentialProvider tokenCredentialProvider = null;
        try {
            tokenCredentialProvider = context.getBean(TokenCredentialProvider.class);
        } catch (NoUniqueBeanDefinitionException e) {
            LOGGER.error("Failed to find unique TokenCredentialProvider Bean for authentication.", e);
            if (properties.isFailFast()) {
                throw e;
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("No TokenCredentialProvider found.");
        }
        return new ClientStore(appProperties, pool, tokenCredentialProvider);
    }
}
