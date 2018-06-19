/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.CredentialsProvider;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureContextAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzurePropertiesConfigured() {
        this.contextRunner
                .withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
                .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
                .withPropertyValues("spring.cloud.azure.region=westUS")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureProperties.class);
                    assertThat(context.getBean(AzureProperties.class).getCredentialFilePath()).isEqualTo("credential");
                    assertThat(context.getBean(AzureProperties.class).getResourceGroup()).isEqualTo("group1");
                    assertThat(context.getBean(AzureProperties.class).getRegion()).isEqualTo("westUS");
                });
    }

    @Test
    public void testWithoutAzureProperties() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AzureProperties.class);
                });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public CredentialsProvider credentialsProvider() {
            return mock(CredentialsProvider.class);
        }

        @Bean
        Azure azure() {
            return mock(Azure.class);
        }

        @Bean
        AzureAdmin azureAdmin() {
            return mock(AzureAdmin.class);
        }
    }
}
