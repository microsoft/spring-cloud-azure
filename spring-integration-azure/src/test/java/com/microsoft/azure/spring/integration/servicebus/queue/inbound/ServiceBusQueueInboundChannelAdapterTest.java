/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue.inbound;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.InboundChannelAdapterTest;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueInboundChannelAdapterTest
        extends InboundChannelAdapterTest<IMessage, UUID, ServiceBusQueueInboundChannelAdapter> {

    @Mock
    private ServiceBusQueueOperation queueOperation;

    @Before
    public void setUp() {
        this.clazz = UUID.class;
        this.checkpointer = (Checkpointer<UUID>) mock(Checkpointer.class);
        this.adapter = new ServiceBusQueueInboundChannelAdapter(destination, queueOperation);
        this.messages = Arrays.stream(payloads).map(this::toMessage).collect(Collectors.toList());
        when(this.queueOperation.getCheckpointer(eq(destination))).thenReturn(this.checkpointer);
    }

    private IMessage toMessage(String payload) {
        IMessage message = mock(IMessage.class);
        when(message.getBody()).thenReturn(payload.getBytes());
        when(message.getLockToken()).thenReturn(UUID.randomUUID());

        return message;
    }
}

