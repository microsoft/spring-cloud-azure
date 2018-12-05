/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@PropertySource(value = "classpath:telemetry.config")
@EnableConfigurationProperties(TelemetryProperties.class)
@ConditionalOnProperty(name = "spring.cloud.azure.telemetry.enabled", matchIfMissing = true)
@ConditionalOnClass(TelemetryClient.class)
public class TelemetryAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "telemetry.instrumentationKey")
    public TelemetrySender telemetrySender(TelemetryProperties telemetryProperties) {
        try {
            return new TelemetrySender(telemetryProperties.getInstrumentationKey(), TelemetryCollector.getInstance());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument to build telemetry tracker");
            return null;
        }
    }
}
