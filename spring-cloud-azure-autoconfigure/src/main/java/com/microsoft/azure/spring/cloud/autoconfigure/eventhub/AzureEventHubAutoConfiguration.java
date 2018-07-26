/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.management.eventhub.AuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationKey;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.eventhub.DefaultEventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.EventHubTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(EventHubClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhub.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureEventHubProperties.class)
public class AzureEventHubAutoConfiguration {
    private static final String EVENT_HUB = "EventHub";

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, EVENT_HUB);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory clientFactory(AzureAdmin azureAdmin, AzureEventHubProperties eventHubProperties) {
        DefaultEventHubClientFactory clientFactory =
                new DefaultEventHubClientFactory(azureAdmin, eventHubProperties.getNamespace());
        clientFactory.initCheckpointConnectionString(eventHubProperties.getCheckpointStorageAccount());
        return clientFactory;
    }
}
