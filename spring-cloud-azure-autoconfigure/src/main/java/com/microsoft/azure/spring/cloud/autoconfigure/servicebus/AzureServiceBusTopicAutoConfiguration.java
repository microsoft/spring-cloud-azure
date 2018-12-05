/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass(TopicClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusTopicAutoConfiguration {
    private static final String SERVICE_BUS_TOPIC = "ServiceBusTopic";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_TOPIC);
    }

    @Bean
    @ConditionalOnMissingBean({ResourceManagerProvider.class, ServiceBusTopicClientFactory.class})
    public ServiceBusTopicClientFactory topicClientFactory(AzureServiceBusProperties serviceBusProperties) {
        return new DefaultServiceBusTopicClientFactory(serviceBusProperties.getConnectionString());
    }

    @Bean
    @ConditionalOnBean(ResourceManagerProvider.class)
    @ConditionalOnMissingBean
    public ServiceBusTopicClientFactory topicClientFactory(ResourceManagerProvider resourceManagerProvider,
            AzureServiceBusProperties serviceBusProperties) {
        DefaultServiceBusTopicClientFactory clientFactory =
                new DefaultServiceBusTopicClientFactory(serviceBusProperties.getConnectionString());
        if (StringUtils.hasText(serviceBusProperties.getNamespace())) {
            clientFactory.setNamespace(serviceBusProperties.getNamespace());
        }
        clientFactory.setResourceManagerProvider(resourceManagerProvider);

        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicOperation topicOperation(ServiceBusTopicClientFactory factory) {
        return new ServiceBusTopicTemplate(factory);
    }
}
