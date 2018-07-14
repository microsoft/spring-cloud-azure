/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.AzureUtil;
import com.microsoft.azure.spring.cloud.storage.AzureStorageProtocolResolver;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
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

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass({CloudBlobClient.class, AzureStorageProtocolResolver.class})
@ConditionalOnProperty(name = "spring.cloud.azure.storage.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureStorageProperties.class)
@Import(AzureStorageProtocolResolver.class)
public class AzureStorageAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageAutoConfiguration.class);

    private static final String STORAGE_BLOB = "StorageBlob";

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @Bean
    @ConditionalOnMissingBean
    public CloudStorageAccount storage(AzureAdmin azureAdmin, AzureStorageProperties storageProperties) {
        TelemetryTracker.triggerEvent(telemetryTracker, STORAGE_BLOB);
        String accountName = storageProperties.getAccount();

        StorageAccount storageAccount = azureAdmin.getOrCreateStorageAccount(accountName);

        String connectionString = AzureUtil.getConnectionString(storageAccount);

        try {
            return CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException | InvalidKeyException e) {
            LOGGER.error("Failed to parse connection string" + connectionString, e);
            throw new RuntimeException("Failed to parse connection string" + connectionString, e);
        }
    }
}
