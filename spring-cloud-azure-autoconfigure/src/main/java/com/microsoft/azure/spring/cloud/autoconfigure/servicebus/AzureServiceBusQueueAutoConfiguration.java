/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.microsoft.azure.spring.cloud.autoconfigure.common.AutoConfigureUtils.getServiceName;

/**
 * An auto-configuration for Service Bus queue
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(QueueClient.class)
@ConditionalOnProperty("spring.cloud.azure.servicebus.namespace")
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusQueueAutoConfiguration {

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, getServiceName(AzureServiceBusQueueAutoConfiguration.class));
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueClientFactory queueClientFactory(AzureAdmin azureAdmin,
            AzureServiceBusProperties serviceBusProperties) {
        return new DefaultServiceBusQueueClientFactory(azureAdmin, serviceBusProperties.getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueOperation queueOperation(ServiceBusQueueClientFactory factory) {
        return new ServiceBusQueueTemplate(factory);
    }
}
