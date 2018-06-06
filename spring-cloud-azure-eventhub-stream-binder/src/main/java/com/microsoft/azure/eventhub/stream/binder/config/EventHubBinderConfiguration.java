/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder.config;

import com.microsoft.azure.eventhub.stream.binder.EventHubMessageChannelBinder;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubExtendedBindingProperties;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import eventhub.core.DefaultEventHubClientFactory;
import eventhub.core.EventHubClientFactory;
import eventhub.core.EventHubOperation;
import eventhub.core.EventHubTemplate;
import eventhub.integration.AzureAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties(EventHubExtendedBindingProperties.class)
public class EventHubBinderConfiguration {

    @Bean
    public EventHubClientFactory clientFactory(AzureAdmin azureAdmin,
            EventHubExtendedBindingProperties bindingProperties) {
        DefaultEventHubClientFactory clientFactory =
                new DefaultEventHubClientFactory(azureAdmin, bindingProperties.getNamespace());
        clientFactory.initCheckpointConnectionString(bindingProperties.getCheckpointStorageAccount());
        clientFactory.setCheckpointStorageAccountContainer(bindingProperties.getCheckpointStorageAccountContainer());
        return clientFactory;
    }

    @Bean
    public EventHubChannelProvisioner eventHubChannelProvisioner(AzureAdmin azureAdmin,
            EventHubExtendedBindingProperties bindingProperties) {
        return new EventHubChannelProvisioner(azureAdmin, bindingProperties.getNamespace());
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
