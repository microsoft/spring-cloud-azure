/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.config;

import com.microsoft.azure.servicebus.stream.binder.ServiceBusTopicMessageChannelBinder;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicChannelProvisioner;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
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
@EnableConfigurationProperties({AzureServiceBusProperties.class, ServiceBusExtendedBindingProperties.class})
public class ServiceBusTopicBinderConfiguration {

    private static final String SERVICE_BUS_TOPIC_BINDER = "ServiceBusTopicBinder";

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, SERVICE_BUS_TOPIC_BINDER);
    }

    @Bean
    public ServiceBusTopicChannelProvisioner serviceBusChannelProvisioner(AzureAdmin azureAdmin,
            AzureServiceBusProperties serviceBusProperties) {
        return new ServiceBusTopicChannelProvisioner(azureAdmin, serviceBusProperties.getNamespace());
    }

    @Bean
    public ServiceBusTopicMessageChannelBinder serviceBusTopicBinder(
            ServiceBusTopicChannelProvisioner topicChannelProvisioner,
            ServiceBusTopicOperation serviceBusTopicOperation) {
        return new ServiceBusTopicMessageChannelBinder(null, topicChannelProvisioner, serviceBusTopicOperation);
    }
}
