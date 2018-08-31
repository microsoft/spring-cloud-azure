/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.spring.integration.InboundChannelAdapterTest;
import com.microsoft.azure.spring.integration.core.support.ServiceBusQueueTestOperation;
import com.microsoft.azure.spring.integration.core.support.ServiceBusTopicTestOperation;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusQueueInboundChannelAdapter> {

    @Mock
    ServiceBusQueueClientFactory clientFactory;

    @Mock
    IQueueClient queueClient;

    @Override
    public void setUp() {
        when(this.clientFactory.getQueueClientCreator()).thenReturn((s) -> queueClient);
        this.adapter =
                new ServiceBusQueueInboundChannelAdapter(destination, new ServiceBusQueueTestOperation(clientFactory));
    }
}
