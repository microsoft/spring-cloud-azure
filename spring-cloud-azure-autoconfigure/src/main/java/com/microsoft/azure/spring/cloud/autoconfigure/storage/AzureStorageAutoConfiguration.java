/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.AzureUtil;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

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
    private static final Log LOGGER = LogFactory.getLog(AzureStorageAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public CloudStorageAccount storage(Azure azure, AzureProperties azureProperties,
                                       AzureStorageProperties azureStorageProperties) {
        String accountName = azureStorageProperties.getAccount();

        StorageAccount storageAccount =
                azure.storageAccounts().getByResourceGroup(azureProperties.getResourceGroup(), accountName);

        String connectionString = AzureUtil.getConnectionString(storageAccount);

        try {
            return CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException | InvalidKeyException e) {
            LOGGER.error("Failed to parse connection string" + connectionString, e);
            throw new RuntimeException("Failed to parse connection string" + connectionString, e);
        }
    }
}
