/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.cosmosdb;

import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass
@ConditionalOnProperty(name = "spring.cloud.azure.cosmosdb.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureCosmosDbProperties.class)
public class AzureCosmosDbAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    CosmosDBAccount cosmosDBAccount(AzureAdmin azureAdmin, AzureCosmosDbProperties azureCosmosDbProperties) {
        String accountName = azureCosmosDbProperties.getAccountName();
        CosmosDBAccount cosmosDBAccount = azureAdmin.getOrCreateCosmosDBAccount(accountName);
        return cosmosDBAccount;
    }
}
