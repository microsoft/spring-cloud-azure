/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.spring.integration.InboundChannelAdapterTest;
import com.microsoft.azure.spring.integration.core.support.EventHubTestOperation;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHubInboundAdapterTest extends InboundChannelAdapterTest<EventHubInboundChannelAdapter> {

    @Override
    public void setUp() {
        this.adapter = new EventHubInboundChannelAdapter(destination, new EventHubTestOperation(), consumerGroup);
    }
}
