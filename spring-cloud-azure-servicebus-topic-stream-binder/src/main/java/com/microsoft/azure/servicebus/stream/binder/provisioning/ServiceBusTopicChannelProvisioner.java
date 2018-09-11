/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.provisioning;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>> {

    private final ResourceManagerProvider resourceManagerProvider;
    private final String namespace;

    public ServiceBusTopicChannelProvisioner(@NonNull ResourceManagerProvider resourceManagerProvider,
            String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.resourceManagerProvider = resourceManagerProvider;
        this.namespace = namespace;
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<ServiceBusProducerProperties> properties) throws ProvisioningException {
        ServiceBusNamespace namespace =
                this.resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(this.namespace);
        this.resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of(namespace, name));

        return new ServiceBusTopicProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) throws ProvisioningException {
        ServiceBusNamespace namespace =
                this.resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(this.namespace);
        Topic topic = this.resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of(namespace, name));
        if (topic == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }

        this.resourceManagerProvider.getServiceBusTopicSubscriptionManager().getOrCreate(Tuple.of(topic, group));
        return new ServiceBusTopicConsumerDestination(name);
    }
}
