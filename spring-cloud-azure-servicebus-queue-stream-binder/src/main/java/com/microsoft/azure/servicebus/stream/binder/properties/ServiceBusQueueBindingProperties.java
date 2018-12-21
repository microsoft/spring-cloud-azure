/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.properties;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueBindingProperties {
    private ServiceBusQueueConsumerProperties consumer = new ServiceBusQueueConsumerProperties();
    private ServiceBusQueueProducerProperties producer = new ServiceBusQueueProducerProperties();

    public ServiceBusQueueConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(ServiceBusQueueConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public ServiceBusQueueProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(ServiceBusQueueProducerProperties producer) {
        this.producer = producer;
    }
}
