/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.outbound;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.expression.Expression;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubMessageHandlerTest {

    @Mock
    private EventHubOperation eventHubOperation;

    private EventHubMessageHandler handler;
    private Message<?> message;
    private String eventHubName = "eventhub";
    private String payload = "payload";

    @Before
    public void setUp() {
        this.message = new GenericMessage<>("testPayload", ImmutableMap.of("key1", "value1", "key2", "value2"));
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.eventHubOperation.sendAsync(eq(eventHubName), isA(EventData.class), isA(PartitionSupplier.class)))
                .thenReturn(future);
        this.handler = new EventHubMessageHandler(this.eventHubName, this.eventHubOperation);

    }

    @Test
    public void testSend() {
        this.handler.handleMessage(this.message);
        verify(this.eventHubOperation, times(1))
                .sendAsync(eq(eventHubName), isA(EventData.class), isA(PartitionSupplier.class));
    }

    @Test
    public void testSendDynamicTopic() {
        String dynamicEventHubName = "dynamicName";
        Message<?> dynamicMessage =
                new GenericMessage<>(payload, ImmutableMap.of(AzureHeaders.NAME, dynamicEventHubName));
        this.handler.handleMessage(dynamicMessage);
        verify(this.eventHubOperation, times(1))
                .sendAsync(eq(dynamicEventHubName), isA(EventData.class), isA(PartitionSupplier.class));
    }

    @Test
    public void testSendSync() {
        this.handler.setSync(true);
        Expression timeout = spy(this.handler.getSendTimeoutExpression());
        this.handler.setSendTimeoutExpression(timeout);

        this.handler.handleMessage(this.message);
        verify(timeout, times(1)).getValue(eq(null), eq(this.message), eq(Long.class));
    }

    @Test
    public void testSendCallback() {
        ListenableFutureCallback<Void> callbackSpy = spy(new ListenableFutureCallback<Void>() {
            @Override
            public void onFailure(Throwable ex) {

            }

            @Override
            public void onSuccess(Void v) {

            }
        });

        this.handler.setSendCallback(callbackSpy);

        this.handler.handleMessage(this.message);

        verify(callbackSpy, times(1)).onSuccess(eq(null));
    }
}
