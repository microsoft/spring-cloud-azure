/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cosmosdb;

import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.management.cosmosdb.DatabaseAccountConnectionString;
import com.microsoft.azure.management.cosmosdb.DatabaseAccountKind;
import com.microsoft.azure.management.cosmosdb.implementation.DatabaseAccountListConnectionStringsResult;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureCosmosDbMongodbAutoConfigurationTest {
    private static final String URI = "test";
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureCosmosDbMongodbAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureCosmosDbMongodbDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.cosmosdb.mongodb.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosDbProperties.class));
    }

    @Test
    public void testWithoutCosmosDbMongodbOperationsClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(MongoOperations.class))
                .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosDbProperties.class));
    }

    @Test
    public void testAzureCosmosDbMongodbPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.cosmosdb.mongodb.account-name=abcd")
                .withPropertyValues("spring.cloud.azure.cosmosdb.mongodb.database=dbname")
                .run(context -> {
            assertThat(context).hasSingleBean(AzureCosmosDbProperties.class);
            assertThat(context.getBean(AzureCosmosDbProperties.class).getAccountName()).isEqualTo("abcd");
            assertThat(context.getBean(AzureCosmosDbProperties.class).getDatabase()).isEqualTo("dbname");
            assertThat(context).hasSingleBean(MongoProperties.class);
            assertThat(context.getBean(MongoProperties.class).getUri()).isEqualTo(URI);
            assertThat(context.getBean(MongoProperties.class).getDatabase()).isEqualTo("dbname");
        });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        AzureAdmin azureAdmin() {
            AzureAdmin azureAdmin = mock(AzureAdmin.class);
            CosmosDBAccount cosmosDBAccount = mock(CosmosDBAccount.class);
            DatabaseAccountListConnectionStringsResult connectionStrings
                    = mock(DatabaseAccountListConnectionStringsResult.class);
            DatabaseAccountConnectionString connectionString = mock(DatabaseAccountConnectionString.class);
            List<DatabaseAccountConnectionString> connectionStringList = new ArrayList();
            connectionStringList.add(connectionString);
            when(cosmosDBAccount.kind()).thenReturn(DatabaseAccountKind.MONGO_DB);
            when(azureAdmin.getOrCreateCosmosDBAccount(isA(String.class),isA(DatabaseAccountKind.class)))
                    .thenReturn(cosmosDBAccount);
            when(cosmosDBAccount.listConnectionStrings()).thenReturn((connectionStrings));
            when(connectionStrings.connectionStrings()).thenReturn(connectionStringList);
            when(connectionStringList.get(0).connectionString()).thenReturn(URI);
            return azureAdmin;
        }
    }
}
