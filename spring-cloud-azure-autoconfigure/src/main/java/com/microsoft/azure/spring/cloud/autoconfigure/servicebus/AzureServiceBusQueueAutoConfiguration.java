/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Service Bus queue
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass(QueueClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusQueueAutoConfiguration {
    private static final String SERVICE_BUS_QUEUE = "ServiceBusQueue";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean({ResourceManagerProvider.class, ServiceBusQueueClientFactory.class})
    public ServiceBusQueueClientFactory queueClientFactory(AzureServiceBusProperties serviceBusProperties) {
        return new DefaultServiceBusQueueClientFactory(serviceBusProperties.getConnectionString());
    }

    @Bean
    @ConditionalOnBean(ResourceManagerProvider.class)
    @ConditionalOnMissingBean
    public ServiceBusQueueClientFactory queueClientFactoryWithResourceManagerProvider(
            ResourceManagerProvider resourceManagerProvider, AzureServiceBusProperties serviceBusProperties) {
        DefaultServiceBusQueueClientFactory clientFactory =
                new DefaultServiceBusQueueClientFactory(serviceBusProperties.getConnectionString());
        if (StringUtils.hasText(serviceBusProperties.getNamespace())) {
            clientFactory.setNamespace(serviceBusProperties.getNamespace());
        }
        clientFactory.setResourceManagerProvider(resourceManagerProvider);

        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueOperation queueOperation(ServiceBusQueueClientFactory factory) {
        return new ServiceBusQueueTemplate(factory);
    }
}
