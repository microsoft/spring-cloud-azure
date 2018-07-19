/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cosmosdb;

import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureCosmosDbMongodbAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureCosmosDbMongodbAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureCosmosDbPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.cosmosdb.mongodb.account-name=abcd").run(context -> {
            assertThat(context).hasSingleBean(AzureCosmosDbMongodbProperties.class);
            assertThat(context.getBean(AzureCosmosDbMongodbProperties.class).getAccountName()).isEqualTo("abcd");
        });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        AzureAdmin azureAdmin() {
            return mock(AzureAdmin.class);
        }

        @Bean
        CosmosDBAccount cosmosDBAccount() {
            return mock(CosmosDBAccount.class);
        }

    }
}
