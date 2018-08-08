/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.azure.spring.integration.SubscribeByGroupOperationTest;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicTemplateSubscribeTest
        extends SubscribeByGroupOperationTest<IMessage, UUID, ServiceBusTopicOperation> {

    @Mock
    private ServiceBusTopicClientFactory mockClientFactory;

    @Mock
    private SubscriptionClient subscriptionClient;

    @Mock
    private SubscriptionClient anotherSubscriptionClient;

    @Before
    public void setUp() {
        this.subscribeByGroupOperation = new ServiceBusTopicTemplate(mockClientFactory);
        when(this.mockClientFactory.getSubscriptionClientCreator()).thenReturn(this::createSubscriptionClient);
        whenRegisterMessageHandler(this.subscriptionClient);
        whenRegisterMessageHandler(this.anotherSubscriptionClient);
    }

    @Override
    protected void verifySubscriberCreatorCalled(int times) {
        verify(this.mockClientFactory, times(times)).getSubscriptionClientCreator();
    }

    private SubscriptionClient createSubscriptionClient(Tuple<String, String> nameAndConsumerGroup) {
        if (nameAndConsumerGroup.getSecond().equals(this.consumerGroup)) {
            return this.subscriptionClient;
        } else {
            return this.anotherSubscriptionClient;
        }
    }

    private void whenRegisterMessageHandler(SubscriptionClient subscriptionClient) {
        try {
            doNothing().when(subscriptionClient).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
