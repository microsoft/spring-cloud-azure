/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureStorageAutoConfigurationTest {
    private ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AzureStorageAutoConfiguration.class))
                                          .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureStorageDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureStorageProperties.class));
    }

    @Test
    public void testWithoutStorageClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(CloudStorageAccount.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureStorageProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureStoragePropertiesIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=a")
                .run(context -> context.getBean(AzureStorageProperties.class));
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
        CloudStorageAccount cloudStorageAccount() {
            return mock(CloudStorageAccount.class);
        }

    }
}
