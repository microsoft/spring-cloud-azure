/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureAdmin;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.microsoft.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass({CloudQueueClient.class, StorageQueueClientFactory.class})
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageQueueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    StorageQueueClientFactory storageQueueFactory(AzureAdmin azureAdmin,
                                                  AzureStorageProperties azureStorageProperties) {
        return new DefaultStorageQueueClientFactory(azureAdmin, azureStorageProperties.getAccount());
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueOperation storageQueueOperation(StorageQueueClientFactory storageQueueClientFactory){
        return new StorageQueueTemplate(storageQueueClientFactory);
    }

}
