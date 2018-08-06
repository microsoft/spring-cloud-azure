/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.MessageHandlerTest;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.servicebus.outbound.ServiceBusMessageHandler;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusMessageHandlerTest extends MessageHandlerTest<IMessage, ServiceBusQueueOperation> {

    @Before
    @Override
    public void setUp() {
        this.messageClass = IMessage.class;
        this.future.complete(null);
        this.sendOperation = mock(ServiceBusQueueOperation.class);
        when(this.sendOperation.sendAsync(eq(this.destination), isA(messageClass), isA(PartitionSupplier.class)))
                .thenReturn(future);
        this.handler = new ServiceBusMessageHandler(this.destination, this.sendOperation);
    }
}
