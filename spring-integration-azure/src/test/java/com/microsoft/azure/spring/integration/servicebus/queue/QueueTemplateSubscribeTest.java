/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.SubscribeOperationTest;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueTemplateSubscribeTest extends SubscribeOperationTest<ServiceBusQueueOperation> {

    @Mock
    private ServiceBusQueueClientFactory mockClientFactory;

    @Mock
    private QueueClient queueClient;

    @Before
    public void setUp() {
        this.subscribeOperation = new ServiceBusQueueTemplate(mockClientFactory);
        when(this.mockClientFactory.getQueueClientCreator()).thenReturn((s) -> queueClient);
        whenRegisterMessageHandler(this.queueClient);
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).getQueueClientCreator();
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).getQueueClientCreator();
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        try {
            verify(this.queueClient, times(times)).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
    }

    private void whenRegisterMessageHandler(QueueClient queueClient) {
        try {
            doNothing().when(queueClient).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
