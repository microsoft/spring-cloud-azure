/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.spring.cloud.autoconfigure.cache.AzureRedisProperties;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
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

    private static String subscriptionId = "id";

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
    public void testAzurePropertiesTelemetryConfigured() {
        this.contextRunner.withPropertyValues("telemetry.instrumentationKey=012345678901234567890123456789012345")
                          .run(context -> assertThat(context.getBean(TelemetryTracker.class)).isNotNull());
    }

    @Test
    public void testAzureTelemetryDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.telemetry.enable=false")
                          .run(context -> assertThat(context).doesNotHaveBean(TelemetryTracker.class));
    }

    @Test
    public void testWithoutTelemetryClientClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(TelemetryClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        Azure azure() {
            final Azure azure = mock(Azure.class);
            final Subscription subscription = mock(Subscription.class);

            when(azure.getCurrentSubscription()).thenReturn(subscription);
            when(subscription.subscriptionId()).thenReturn(subscriptionId);

            return azure;
        }
    }
}
