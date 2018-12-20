/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.config;

import com.microsoft.azure.servicebus.stream.binder.ServiceBusTopicMessageChannelBinder;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicChannelProvisioner;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicChannelResourceManagerProvisioner;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@EnableConfigurationProperties({AzureServiceBusProperties.class, ServiceBusExtendedBindingProperties.class})
public class ServiceBusTopicBinderConfiguration {

    private static final String SERVICE_BUS_TOPIC_BINDER = "ServiceBusTopicBinder";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_TOPIC_BINDER);
    }

    @Bean
    @ConditionalOnBean(ResourceManagerProvider.class)
    @ConditionalOnMissingBean
    public ServiceBusTopicChannelProvisioner serviceBusChannelProvisioner(
            AzureServiceBusProperties serviceBusProperties) {
        if (this.resourceManagerProvider != null) {
            return new ServiceBusTopicChannelResourceManagerProvisioner(resourceManagerProvider,
                    serviceBusProperties.getNamespace());
        }
        return new ServiceBusTopicChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean({ResourceManagerProvider.class, ServiceBusTopicChannelProvisioner.class})
    public ServiceBusTopicChannelProvisioner serviceBusChannelProvisionerWithResourceManagerProvider() {
        return new ServiceBusTopicChannelProvisioner();
    }

    @Bean
    public ServiceBusTopicMessageChannelBinder serviceBusTopicBinder(
            ServiceBusTopicChannelProvisioner topicChannelProvisioner,
            ServiceBusTopicOperation serviceBusTopicOperation, ServiceBusExtendedBindingProperties bindingProperties) {
        ServiceBusTopicMessageChannelBinder binder =
                new ServiceBusTopicMessageChannelBinder(null, topicChannelProvisioner, serviceBusTopicOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
