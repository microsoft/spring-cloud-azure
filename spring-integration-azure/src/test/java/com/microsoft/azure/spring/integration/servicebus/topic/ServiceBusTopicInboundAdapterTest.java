/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.spring.integration.InboundChannelAdapterTest;
import com.microsoft.azure.spring.integration.core.support.ServiceBusTopicTestOperation;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusTopicInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusTopicInboundChannelAdapter> {

    @Override
    public void setUp() {
        this.adapter = new ServiceBusTopicInboundChannelAdapter(destination, new ServiceBusTopicTestOperation(),
                consumerGroup);
    }
}
