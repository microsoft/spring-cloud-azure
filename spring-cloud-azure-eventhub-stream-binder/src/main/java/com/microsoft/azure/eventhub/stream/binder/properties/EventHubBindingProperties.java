/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder.properties;

/**
 * @author Warren Zhu
 */
public class EventHubBindingProperties {
    private EventHubConsumerProperties consumer = new EventHubConsumerProperties();
    private EventHubProducerProperties producer = new EventHubProducerProperties();

    public EventHubConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(EventHubConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public EventHubProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(EventHubProducerProperties producer) {
        this.producer = producer;
    }
}
