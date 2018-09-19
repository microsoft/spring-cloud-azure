/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.integration.eventhub.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureBefore(TelemetryAutoConfiguration.class)
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(EventHubClient.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhub", value = {"namespace", "checkpoint-storage-account"})
@EnableConfigurationProperties(AzureEventHubProperties.class)
public class AzureEventHubAutoConfiguration {
    private static final String EVENT_HUB = "EventHub";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(EVENT_HUB);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory clientFactory(ResourceManagerProvider resourceManagerProvider,
            AzureEventHubProperties eventHubProperties) {
        EventHubNamespace namespace =
                resourceManagerProvider.getEventHubNamespaceManager().getOrCreate(eventHubProperties.getNamespace());
        EventHubConnectionStringProvider provider = new EventHubConnectionStringProvider(namespace);
        return new DefaultEventHubClientFactory(resourceManagerProvider,
                eventHubProperties.getCheckpointStorageAccount(), provider::getConnectionString);
    }
}
