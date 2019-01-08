/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicBindingProperties implements BinderSpecificPropertiesProvider {
    private ServiceBusTopicConsumerProperties consumer = new ServiceBusTopicConsumerProperties();
    private ServiceBusTopicProducerProperties producer = new ServiceBusTopicProducerProperties();

    public ServiceBusTopicConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(ServiceBusTopicConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public ServiceBusTopicProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(ServiceBusTopicProducerProperties producer) {
        this.producer = producer;
    }
}
