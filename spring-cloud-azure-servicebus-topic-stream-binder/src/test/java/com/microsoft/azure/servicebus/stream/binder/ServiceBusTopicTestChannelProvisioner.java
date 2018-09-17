/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicChannelProvisioner;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicConsumerDestination;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicProducerDestination;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicTestChannelProvisioner extends ServiceBusTopicChannelProvisioner {

    public ServiceBusTopicTestChannelProvisioner(ResourceManagerProvider resourceManagerProvider, String namespace) {
        super(resourceManagerProvider, namespace);
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<ServiceBusProducerProperties> properties) throws ProvisioningException {
        return new ServiceBusTopicProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) throws ProvisioningException {
        return new ServiceBusTopicConsumerDestination(name);
    }
}
