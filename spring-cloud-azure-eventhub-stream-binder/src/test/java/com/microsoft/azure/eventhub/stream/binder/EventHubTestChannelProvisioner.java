/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubConsumerDestination;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubProducerDestination;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

/**
 * @author Warren Zhu
 */
public class EventHubTestChannelProvisioner extends EventHubChannelProvisioner {

    public EventHubTestChannelProvisioner(AzureAdmin azureAdmin, String namespace) {
        super(azureAdmin, namespace);
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<EventHubProducerProperties> properties) throws ProvisioningException {
        return new EventHubProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<EventHubConsumerProperties> properties) throws ProvisioningException {
        return new EventHubConsumerDestination(name);
    }
}
