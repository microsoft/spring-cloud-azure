/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;


import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.microsoft.azure.spring.integration.storage.queue.factory.DefaultStorageQueueFactory;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.cloud.azure.storage.queue.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageQueueAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    StorageQueueFactory storageQueueFactory(AzureAdmin azureAdmin, AzureStorageProperties azureStorageProperties) {
        return new DefaultStorageQueueFactory(azureAdmin, azureStorageProperties.getAccount());
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueOperation storageQueueOperation(StorageQueueFactory storageQueueFactory){
        return new StorageQueueTemplate(storageQueueFactory);
    }

}
