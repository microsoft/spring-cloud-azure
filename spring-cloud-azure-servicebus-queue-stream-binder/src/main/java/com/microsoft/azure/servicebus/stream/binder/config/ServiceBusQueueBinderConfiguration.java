/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.config;

import com.microsoft.azure.servicebus.stream.binder.ServiceBusQueueMessageChannelBinder;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusQueueChannelProvisioner;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusQueueChannelResourceManagerProvisioner;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.ServiceBusUtils;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
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
@EnableConfigurationProperties({AzureServiceBusProperties.class, ServiceBusQueueExtendedBindingProperties.class})
public class ServiceBusQueueBinderConfiguration {

    private static final String SERVICE_BUS_QUEUE_BINDER = "ServiceBusQueueBinder";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_QUEUE_BINDER);
    }

    @Bean
    @ConditionalOnBean(ResourceManagerProvider.class)
    @ConditionalOnMissingBean
    public ServiceBusQueueChannelProvisioner serviceBusChannelProvisioner(
            AzureServiceBusProperties serviceBusProperties) {
        if (this.resourceManagerProvider != null) {
            return new ServiceBusQueueChannelResourceManagerProvisioner(resourceManagerProvider,
                    serviceBusProperties.getNamespace());
        } else {
            TelemetryCollector.getInstance().addProperty(SERVICE_BUS_QUEUE_BINDER, NAMESPACE,
                    ServiceBusUtils.getNamespace(serviceBusProperties.getConnectionString()));
        }
        return new ServiceBusQueueChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean({ResourceManagerProvider.class, ServiceBusQueueChannelProvisioner.class})
    public ServiceBusQueueChannelProvisioner serviceBusChannelProvisionerWithResourceManagerProvider() {
        return new ServiceBusQueueChannelProvisioner();
    }

    @Bean
    public ServiceBusQueueMessageChannelBinder serviceBusQueueBinder(
            ServiceBusQueueChannelProvisioner queueChannelProvisioner,
            ServiceBusQueueOperation serviceBusQueueOperation, ServiceBusQueueExtendedBindingProperties
            bindingProperties) {
        ServiceBusQueueMessageChannelBinder binder =
                new ServiceBusQueueMessageChannelBinder(null, queueChannelProvisioner, serviceBusQueueOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
