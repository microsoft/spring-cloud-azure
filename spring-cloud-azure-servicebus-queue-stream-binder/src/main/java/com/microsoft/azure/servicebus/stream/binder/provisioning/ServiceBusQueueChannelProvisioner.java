/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.provisioning;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<ServiceBusQueueConsumerProperties>,
                ExtendedProducerProperties<ServiceBusQueueProducerProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<ServiceBusQueueProducerProperties> properties) throws ProvisioningException {
        validateOrCreateForProducer(name);
        return new ServiceBusQueueProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<ServiceBusQueueConsumerProperties> properties) throws ProvisioningException {
        validateOrCreateForConsumer(name, group);
        return new ServiceBusQueueConsumerDestination(name);
    }

    protected void validateOrCreateForConsumer(String name, String group) {
        // no-op
    }

    protected void validateOrCreateForProducer(String name) {
        // no-op
    }
}
