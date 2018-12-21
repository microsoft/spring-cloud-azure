/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusQueueChannelProvisioner;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueTestBinder extends
        AbstractTestBinder<ServiceBusQueueMessageChannelBinder,
                ExtendedConsumerProperties<ServiceBusQueueConsumerProperties>,
                ExtendedProducerProperties<ServiceBusQueueProducerProperties>> {

    ServiceBusQueueTestBinder(ServiceBusQueueOperation operation) {

        ServiceBusQueueMessageChannelBinder binder =
                new ServiceBusQueueMessageChannelBinder(BinderHeaders.STANDARD_HEADERS,
                        new ServiceBusQueueChannelProvisioner(), operation);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        this.setBinder(binder);
    }

    @Override
    public void cleanup() {
        // No-op
    }

}
