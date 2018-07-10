/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureStorageAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(AzureContextAutoConfiguration.class, AzureStorageAutoConfiguration.class))
                                                                                   .withUserConfiguration(
                                                                                           TestConfiguration.class);

    @Test
    public void testWithoutAzureStorageProperties() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureStorageProperties.class));
    }

    @Test
    public void testAzureStoragePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=acc1").run(context -> {
            assertThat(context).hasSingleBean(AzureStorageProperties.class);
            assertThat(context.getBean(AzureStorageProperties.class).getAccount()).isEqualTo("acc1");
        });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        AzureAdmin azureAdmin() {
            return mock(AzureAdmin.class);
        }

        @Bean
        CloudStorageAccount cloudStorageAccount() {
            return mock(CloudStorageAccount.class);
        }

    }
}
