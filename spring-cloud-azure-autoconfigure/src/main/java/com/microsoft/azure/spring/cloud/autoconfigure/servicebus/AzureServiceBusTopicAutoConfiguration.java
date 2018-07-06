/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryUtils;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(TopicClient.class)
@ConditionalOnProperty("spring.cloud.azure.servicebus.namespace")
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusTopicAutoConfiguration {

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryUtils.telemetryTriggerEvent(telemetryTracker, getClass().getSimpleName());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicClientFactory clientFactory(AzureAdmin azureAdmin, AzureServiceBusProperties
            serviceBusProperties) {
        return new DefaultServiceBusTopicClientFactory(azureAdmin, serviceBusProperties.getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicOperation operation(ServiceBusTopicClientFactory factory){
        return new ServiceBusTopicTemplate(factory);
    }
}
