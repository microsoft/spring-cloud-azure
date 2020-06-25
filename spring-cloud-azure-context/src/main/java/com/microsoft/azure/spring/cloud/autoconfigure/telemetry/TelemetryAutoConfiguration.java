/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryProperties;
import com.microsoft.azure.spring.cloud.telemetry.TelemetrySender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@PropertySource(value = "classpath:telemetry.config")
@EnableConfigurationProperties(TelemetryProperties.class)
@ConditionalOnProperty(name = "spring.cloud.azure.telemetry.enabled", matchIfMissing = true)
@ConditionalOnExpression("'${telemetry.instrumentationKey}' != '@telemetry.instrumentationKey@'")
public class TelemetryAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TelemetryAutoConfiguration.class);

    @Autowired
    private AzureTokenCredentials credentials;

    @Bean
    public TelemetrySender telemetrySender(TelemetryProperties telemetryProperties) {
        try {
            return new TelemetrySender(telemetryProperties.getInstrumentationKey(), TelemetryCollector.getInstance());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument to build telemetry tracker");
            return null;
        }
    }

    @Bean
    public TelemetryCollector telemetryCollector() {
        TelemetryCollector.getInstance().setSubscription(credentials.defaultSubscriptionId());
        return TelemetryCollector.getInstance();
    }
}
