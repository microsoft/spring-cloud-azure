/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.CredentialsProvider;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


public class TelemetryAutoConfigurationTest {
    private ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(TelemetryAutoConfiguration.class))
                                          .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testTelemetryPropertiesConfigured() {
        this.contextRunner.withPropertyValues("telemetry.instrumentationKey=012345678901234567890123456789012345")
                          .run(context -> {
                              assertThat(context).hasSingleBean(TelemetryProperties.class);
                              assertThat(context.getBean(TelemetryProperties.class).getInstrumentationKey())
                                      .isEqualTo("012345678901234567890123456789012345");
                          });
    }

    @Test
    public void testAzurePropertiesTelemetryMissing() {
        this.contextRunner.withPropertyValues("telemetry.instrumentationKey=012345678901234567890123456789012345")
                          .run(context -> assertThat(context.getBean(TelemetryTracker.class)).isNotNull());
    }

    @Test
    public void testAzurePropertiesTelemetryConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.telemetryAllowed=true")
                          .withPropertyValues("telemetry.instrumentationKey=012345678901234567890123456789012345")

                          .run(context -> assertThat(context.getBean(TelemetryTracker.class)).isNotNull());
    }

    @Test
    public void testAzurePropertiesTelemetryConfiguredException() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.telemetryAllowed=false")
                          .run(context -> assertThat(context).doesNotHaveBean(TelemetryTracker.class));
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
        CredentialsProvider credentialsProvider() {
            return mock(CredentialsProvider.class);
        }

        @Bean
        AzureAdmin azureAdmin() {
            return mock(AzureAdmin.class);
        }

        @Bean
        AzureProperties azureProperties() {
            AzureProperties properties = mock(AzureProperties.class);
            when(properties.getCredentialFilePath()).thenReturn("credential");
            when(properties.getResourceGroup()).thenReturn("resourceGroup");
            when(properties.getRegion()).thenReturn("region");

            return properties;
        }
    }
}
