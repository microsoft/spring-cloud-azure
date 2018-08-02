/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.SendOperationTest;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicTemplateSendTest extends SendOperationTest<IMessage, ServiceBusTopicOperation> {

    @Mock
    private ServiceBusTopicClientFactory mockClientFactory;

    @Mock
    private TopicClient mockClient;

    @Before
    public void setUp() throws ServiceBusException, InterruptedException {
        Mockito.<Function<String, ? extends IMessageSender>>when(this.mockClientFactory.getSenderCreator())
                .thenReturn(s -> this.mockClient);
        when(this.mockClient.sendAsync(isA(IMessage.class))).thenReturn(future);

        this.sendOperation = new ServiceBusTopicTemplate(mockClientFactory);
        this.message = new Message(payload);
    }

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
