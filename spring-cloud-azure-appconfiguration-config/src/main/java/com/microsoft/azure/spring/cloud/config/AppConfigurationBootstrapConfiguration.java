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

import com.microsoft.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;
import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

@Configuration
@EnableConfigurationProperties({ AppConfigurationProperties.class, AppConfigurationProviderProperties.class })
@ConditionalOnClass(AppConfigurationPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AppConfigurationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AppConfigurationBootstrapConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBootstrapConfiguration.class);

    @Bean
    public ConnectionPool initConnectionString(AppConfigurationProperties properties) {
        ConnectionPool pool = new ConnectionPool();
        List<ConfigStore> stores = properties.getStores();

        for (ConfigStore store : stores) {
            if (StringUtils.hasText(store.getEndpoint()) && StringUtils.hasText(store.getConnectionString())) {
                pool.put(store.getEndpoint(), new Connection(store.getConnectionString()));
            } else if (StringUtils.hasText(store.getEndpoint())) {
                AppConfigManagedIdentityProperties msiProps = properties.getManagedIdentity();
                if (msiProps != null && msiProps.getClientId() != null) {
                    pool.put(store.getEndpoint(), new Connection(store.getEndpoint(), msiProps.getClientId()));
                } else {
                    pool.put(store.getEndpoint(), new Connection(store.getEndpoint(), ""));
                }

            }
        }

        Assert.notEmpty(pool.getAll(), "Connection string pool for the configuration stores is empty");

        return pool;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        return HttpClients.createSystem();
    }

    @Bean
    public AppConfigurationPropertySourceLocator sourceLocator(AppConfigurationProperties properties,
            AppConfigurationProviderProperties appProperties, ClientStore clients, ApplicationContext context) {
        KeyVaultCredentialProvider keyVaultCredentialProvider = null;
        try {
            keyVaultCredentialProvider = context.getBean(KeyVaultCredentialProvider.class);
        } catch (NoUniqueBeanDefinitionException e) {
            throw new RuntimeException("Failed to find unique KeyVaultCredentialProvider Bean for authentication.", e);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.debug("No KeyVaultCredentialProvider found.");
        }
        return new AppConfigurationPropertySourceLocator(properties, appProperties, clients,
                keyVaultCredentialProvider);
    }

    @Bean
    public ClientStore buildClientStores(AppConfigurationProperties properties,
            AppConfigurationProviderProperties appProperties, ConnectionPool pool, ApplicationContext context) {
        AppConfigurationCredentialProvider tokenCredentialProvider = null;
        AppConfigurationClientProvider clientProvider = null;
        try {
            tokenCredentialProvider = context.getBean(AppConfigurationCredentialProvider.class);
        } catch (NoUniqueBeanDefinitionException e) {
            throw new RuntimeException(
                    "Failed to find unique AppConfigurationCredentialProvider Bean for authentication.", e);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.debug("No AppConfigurationCredentialProvider found.");
        }
        try {
            clientProvider = context.getBean(AppConfigurationClientProvider.class);
        } catch (NoUniqueBeanDefinitionException e) {
            throw new RuntimeException(
                    "Failed to find unique AppConfigurationClientProvider Bean for credential build.", e);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.debug("No AppConfigurationClientProvider found.");
        }
        return new ClientStore(appProperties, pool, tokenCredentialProvider, clientProvider);
    }
}
