/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.storage.AzureStorageProtocolResolver;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass({CloudStorageAccount.class, AzureStorageProtocolResolver.class})
@ConditionalOnProperty(name = "spring.cloud.azure.storage.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureStorageProperties.class)
@Import(AzureStorageProtocolResolver.class)
public class AzureStorageAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AzureStorageAutoConfiguration.class);
    private static final String STORAGE_BLOB = "Storage";

    @Autowired
    private BeanFactory beanFactory;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE_BLOB);
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudStorageAccount storageAccount(AzureStorageProperties storageProperties, AzureProperties
            azureProperties) {

        String connectionString;

        if (beanFactory.containsBean("resourceManagerProvider")) {
            ResourceManagerProvider resourceManagerProvider = beanFactory.getBean(ResourceManagerProvider.class);
            String accountName = storageProperties.getAccount();

            StorageAccount storageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(accountName);

            connectionString = StorageConnectionStringProvider
                    .getConnectionString(storageAccount, azureProperties.getEnvironment());

        } else {
            connectionString = StorageConnectionStringProvider
                    .getConnectionString(storageProperties.getAccount(), storageProperties.getAccessKey(),
                            azureProperties.getEnvironment());
        }

        try {
            return CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException | InvalidKeyException e) {
            log.error("Failed to parse storage connection string" + connectionString, e);
            throw new RuntimeException("Failed to parse storage connection string" + connectionString, e);
        }
    }
}
