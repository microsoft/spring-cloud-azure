/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder.config;

import com.microsoft.azure.eventhub.stream.binder.EventHubMessageChannelBinder;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubExtendedBindingProperties;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.eventhub.DefaultEventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.EventHubTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@AutoConfigureAfter({AzureContextAutoConfiguration.class, TelemetryAutoConfiguration.class})
@EnableConfigurationProperties({AzureEventHubProperties.class, EventHubExtendedBindingProperties.class})
public class EventHubBinderConfiguration {

    private static final String EVENT_HUB_BINDER = "EventHubBinder";

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, EVENT_HUB_BINDER);
    }

    @Bean
    public EventHubClientFactory clientFactory(AzureAdmin azureAdmin, AzureEventHubProperties eventHubProperties,
            EventHubExtendedBindingProperties bindingProperties) {
        DefaultEventHubClientFactory clientFactory =
                new DefaultEventHubClientFactory(azureAdmin, eventHubProperties.getNamespace());
        clientFactory.initCheckpointConnectionString(bindingProperties.getCheckpointStorageAccount());
        return clientFactory;
    }

    @Bean
    public EventHubChannelProvisioner eventHubChannelProvisioner(AzureAdmin azureAdmin,
            AzureEventHubProperties eventHubProperties) {
        return new EventHubChannelProvisioner(azureAdmin, eventHubProperties.getNamespace());
    }

    @Bean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

    @Bean
    public EventHubMessageChannelBinder eventHubBinder(EventHubChannelProvisioner eventHubChannelProvisioner,
            EventHubOperation eventHubOperation) {
        return new EventHubMessageChannelBinder(null, eventHubChannelProvisioner, eventHubOperation);
    }
}
