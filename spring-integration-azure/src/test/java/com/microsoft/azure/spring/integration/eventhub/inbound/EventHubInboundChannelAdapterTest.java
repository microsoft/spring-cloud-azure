/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.InboundChannelAdapterTest;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventHubInboundChannelAdapterTest
        extends InboundChannelAdapterTest<EventData, EventData, EventHubInboundChannelAdapter> {

    @Mock
    private EventHubOperation eventHubOperation;

    @Before
    public void setUp() {
        this.clazz = EventData.class;
        this.checkpointer = (Checkpointer<EventData>) mock(Checkpointer.class);
        this.adapter = new EventHubInboundChannelAdapter(destination, eventHubOperation, consumerGroup);
        this.messages = Arrays.stream(payloads).map(p -> EventData.create(p.getBytes())).collect(Collectors.toList());
        when(this.eventHubOperation.getCheckpointer(eq(destination), eq(consumerGroup))).thenReturn(this.checkpointer);
    }
}

