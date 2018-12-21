/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.stream.servicebus.queue")
public class ServiceBusQueueExtendedBindingProperties
        implements ExtendedBindingProperties<ServiceBusQueueConsumerProperties, ServiceBusQueueProducerProperties> {
    private final Map<String, ServiceBusQueueBindingProperties> bindings = new ConcurrentHashMap<>();

    @Override
    public ServiceBusQueueConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new ServiceBusQueueBindingProperties()).getConsumer();
    }

    @Override
    public ServiceBusQueueProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new ServiceBusQueueBindingProperties()).getProducer();
    }

    public Map<String, ServiceBusQueueBindingProperties> getBindings() {
        return bindings;
    }
}
