package com.microsoft.azure.spring.cloud.autoconfigure.cosmosdb;

import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageProperties;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    CosmosDBAccount cosmosDBAccount(AzureAdmin azureAdmin, AzureCosmosDbProperties azureCosmosDbProperties){
        String accountName=azureCosmosDbProperties.getAccountName();
        String kind=azureCosmosDbProperties.getKind();
        CosmosDBAccount cosmosDBAccount=azureAdmin.getOrCreateCosmosDBAccount(accountName,kind);
        return cosmosDBAccount;
    }
}
