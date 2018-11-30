/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder.provisioning;

import com.microsoft.azure.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
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
public class EventHubChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<EventHubConsumerProperties>,
                ExtendedProducerProperties<EventHubProducerProperties>> {

    private final ResourceManagerProvider resourceManagerProvider;
    private final String namespace;

    public EventHubChannelProvisioner(@NonNull ResourceManagerProvider resourceManagerProvider,
                                      @NonNull String namespace) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.resourceManagerProvider = resourceManagerProvider;
        this.namespace = namespace;
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<EventHubProducerProperties> properties) throws ProvisioningException {
        EventHubNamespace eventHubNamespace =
                this.resourceManagerProvider.getEventHubNamespaceManager().getOrCreate(namespace);
        this.resourceManagerProvider.getEventHubManager().getOrCreate(Tuple.of(eventHubNamespace, name));

        return new EventHubProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<EventHubConsumerProperties> properties) throws ProvisioningException {
        EventHubNamespace eventHubNamespace =
                this.resourceManagerProvider.getEventHubNamespaceManager().getOrCreate(namespace);
        EventHub eventHub = this.resourceManagerProvider.getEventHubManager().get(Tuple.of(eventHubNamespace,
                name));
        if (eventHub == null) {
            throw new ProvisioningException(
                    String.format("Event hub with name '%s' in namespace '%s' not existed", name, namespace));
        }

        this.resourceManagerProvider.getEventHubConsumerGroupManager().getOrCreate(Tuple.of(eventHub, group));
        return new EventHubConsumerDestination(name);
    }
}
