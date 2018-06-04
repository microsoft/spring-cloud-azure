/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder.provisioning;

import com.microsoft.azure.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubProducerProperties;
import eventhub.integration.AzureAdmin;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class EventHubChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<EventHubConsumerProperties>,
                ExtendedProducerProperties<EventHubProducerProperties>> {

    private final AzureAdmin azureAdmin;
    private final String namespace;

    public EventHubChannelProvisioner(AzureAdmin azureAdmin, String namespace) {
        Assert.notNull(azureAdmin, "The azureAdmin can't be null.");
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.azureAdmin = azureAdmin;
        this.namespace = namespace;
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<EventHubProducerProperties> properties) throws ProvisioningException {
        this.azureAdmin.getOrCreateEventHub(namespace, name);

        return new EventHubProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<EventHubConsumerProperties> properties) throws ProvisioningException {
        //TODO: create consumer group if not existed
        return new EventHubConsumerDestination(name);
    }
}
