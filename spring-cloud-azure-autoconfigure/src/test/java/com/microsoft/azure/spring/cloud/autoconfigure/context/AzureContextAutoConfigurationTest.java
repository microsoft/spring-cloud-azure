/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class AzureContextAutoConfigurationTest {
    private ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class))
                                          .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzurePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
                          .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
                          .withPropertyValues("spring.cloud.azure.region=westUS").run(context -> {
            assertThat(context).hasSingleBean(AzureProperties.class);
            assertThat(context.getBean(AzureProperties.class).getCredentialFilePath()).isEqualTo("credential");
            assertThat(context.getBean(AzureProperties.class).getResourceGroup()).isEqualTo("group1");
            assertThat(context.getBean(AzureProperties.class).getRegion()).isEqualTo("westUS");
        });
    }

    @Test
    public void testAzurePropertiesTelemetryMissing() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
                          .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
                          .run(context -> assertThat(context.getBean(TelemetryTracker.class)).isNotNull());
    }

    @Test
    public void testAzurePropertiesTelemetryConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
                          .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
                          .withPropertyValues("spring.cloud.azure.telemetryAllowed=true")
                          .run(context -> assertThat(context.getBean(TelemetryTracker.class)).isNotNull());
    }

    @Test
    public void testAzurePropertiesTelemetryConfiguredException() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
                          .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
                          .withPropertyValues("spring.cloud.azure.telemetryAllowed=false")
                          .run(context -> assertThat(context).doesNotHaveBean(TelemetryTracker.class));
    }

    @Test
    public void testWithoutAzureProperties() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureProperties.class));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        Azure azure() {
            final Azure azure = mock(Azure.class);
            final Subscription subscription = mock(Subscription.class);

            when(azure.getCurrentSubscription()).thenReturn(subscription);
            when(subscription.subscriptionId()).thenReturn("Fake-Id");

            return azure;
        }

        @Bean
        AzureAdmin azureAdmin() {
            return mock(AzureAdmin.class);
        }
    }
}
