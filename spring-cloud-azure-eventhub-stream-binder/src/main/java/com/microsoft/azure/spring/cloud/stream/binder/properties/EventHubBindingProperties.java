/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.azure.spring.cloud.stream.binder.properties;

/**
 * @author Warren Zhu
 */
public class EventHubBindingProperties {

    private EventHubConsumerProperties consumer = new EventHubConsumerProperties();

    private EventHubProducerProperties producer = new EventHubProducerProperties();

    public EventHubConsumerProperties getConsumer() {
        return this.consumer;
    }

    public void setConsumer(EventHubConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public EventHubProducerProperties getProducer() {
        return this.producer;
    }

    public void setProducer(EventHubProducerProperties producer) {
        this.producer = producer;
    }
}
