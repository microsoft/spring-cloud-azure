/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.stream.servicebus")
public class ServiceBusExtendedBindingProperties
        implements ExtendedBindingProperties<ServiceBusConsumerProperties, ServiceBusProducerProperties> {
    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.servicebus.default";
    private final Map<String, ServiceBusBindingProperties> bindings = new ConcurrentHashMap<>();

    @Override
    public ServiceBusConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new ServiceBusBindingProperties()).getConsumer();
    }

    @Override
    public ServiceBusProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindings.computeIfAbsent(channelName, key -> new ServiceBusBindingProperties()).getProducer();
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return ServiceBusBindingProperties.class;
    }

    public Map<String, ServiceBusBindingProperties> getBindings() {
        return bindings;
    }
}
