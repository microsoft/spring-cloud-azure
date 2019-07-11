/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringBuilder;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.storage.AzureStorageProtocolResolver;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(CloudStorageAccount.class)
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AzureStorageAutoConfiguration.class);
    private static final String STORAGE = "Storage";
    private static final String ACCOUNT_NAME = "AccountName";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;
    @Autowired
    private AzureStorageProperties storageProperties;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE);
    }

    @Bean
    @ConditionalOnMissingBean
    public StorageConnectionStringProvider storageConnectionStringProvider() {
        return new StorageConnectionStringProvider(
                new StorageConnectionStringBuilder(storageProperties.isSecureTransfer()));
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudStorageAccount storageAccount(EnvironmentProvider environmentProvider,
            StorageConnectionStringProvider storageConnectionStringProvider) {
        String connectionString;

        if (resourceManagerProvider != null) {
            String accountName = storageProperties.getAccount();

            StorageAccount storageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(accountName);

            connectionString = storageConnectionStringProvider
                    .getConnectionString(storageAccount, environmentProvider.getEnvironment());

        } else {
            connectionString = storageConnectionStringProvider
                    .getConnectionString(storageProperties.getAccount(), storageProperties.getAccessKey(),
                            environmentProvider.getEnvironment());
            TelemetryCollector.getInstance().addProperty(STORAGE, ACCOUNT_NAME, storageProperties.getAccount());
        }

        try {
            return CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException | InvalidKeyException e) {
            log.error("Failed to parse storage connection string" + connectionString, e);
            throw new RuntimeException("Failed to parse storage connection string" + connectionString, e);
        }
    }

    @Configuration
    @ConditionalOnClass(AzureStorageProtocolResolver.class)
    @Import(AzureStorageProtocolResolver.class)
    class StorageResourceConfiguration {
    }
}
