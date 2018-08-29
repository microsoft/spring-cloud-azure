/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.spring.integration.SendOperationTest;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import org.junit.Before;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public abstract class ServiceBusTemplateSendTest<T extends ServiceBusSenderFactory>
        extends SendOperationTest<SendOperation> {

    protected T mockClientFactory;

    protected IMessageSender mockClient;

    @Before
    public abstract void setUp();

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).sendAsync(isA(IMessage.class));
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getSenderCreator()).thenReturn((s) -> {
            throw new ServiceBusRuntimeException("couldn't create the service bus topic client.");
        });
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getSenderCreator();
    }

    @Override
    protected void verifySendWithPartitionKey(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void verifySendWithPartitionId(int times) {
        verifySendCalled(times);
    }

}
