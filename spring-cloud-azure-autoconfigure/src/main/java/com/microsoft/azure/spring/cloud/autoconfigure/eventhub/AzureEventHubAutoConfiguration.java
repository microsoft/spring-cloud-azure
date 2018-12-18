/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnClass(EventHubClient.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhub", value = {"namespace", "checkpoint-storage-account"})
@EnableConfigurationProperties(AzureEventHubProperties.class)
public class AzureEventHubAutoConfiguration {
    private static final String EVENT_HUB = "EventHub";

    @Autowired
    private BeanFactory beanFactory;

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
    @ConditionalOnBean(ResourceManagerProvider.class)
    public EventHubConnectionStringProvider eventHubConnectionStringProvider(
            AzureEventHubProperties eventHubProperties) {
        if (beanFactory.containsBean("resourceManagerProvider")) {
            ResourceManagerProvider resourceManagerProvider = beanFactory.getBean(ResourceManagerProvider.class);
            EventHubNamespace namespace = resourceManagerProvider.getEventHubNamespaceManager()
                                                                 .getOrCreate(eventHubProperties.getNamespace());
            return new EventHubConnectionStringProvider(namespace);
        } else {
            return new EventHubConnectionStringProvider(eventHubProperties.getConnectionString());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory clientFactory(EventHubConnectionStringProvider connectionStringProvider,
            AzureEventHubProperties eventHubProperties, AzureProperties azureProperties) {

        if (beanFactory.containsBean("resourceManagerProvider")) {
            ResourceManagerProvider resourceManagerProvider = beanFactory.getBean(ResourceManagerProvider.class);
            StorageAccount checkpointStorageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(
                    eventHubProperties.getCheckpointStorageAccount());
            String checkpointConnectionString = StorageConnectionStringProvider
                    .getConnectionString(checkpointStorageAccount, azureProperties.getEnvironment());

            return new DefaultEventHubClientFactory(connectionStringProvider, checkpointConnectionString);
        } else {
            String checkpointConnectionString = StorageConnectionStringProvider
                    .getConnectionString(eventHubProperties.getCheckpointStorageAccount(),
                            eventHubProperties.getCheckpointAccessKey(), azureProperties.getEnvironment());
            return new DefaultEventHubClientFactory(connectionStringProvider, checkpointConnectionString);
        }

    }
}
