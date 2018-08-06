/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.spring.integration.SendOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubTemplateSendTest extends SendOperationTest<EventData, EventHubOperation> {

    @Mock
    private EventHubClientFactory mockClientFactory;

    @Mock
    private EventHubClient mockClient;

    @Mock
    private PartitionSender mockSender;

    @Before
    public void setUp() {

        when(this.mockClientFactory.getEventHubClientCreator()).thenReturn(s -> this.mockClient);
        when(this.mockClient.send(isA(EventData.class))).thenReturn(this.future);
        when(this.mockClient.send(isA(EventData.class), eq(partitionKey))).thenReturn(this.future);

        when(this.mockClientFactory.getPartitionSenderCreator()).thenReturn(t -> this.mockSender);
        when(this.mockSender.send(isA(EventData.class))).thenReturn(this.future);
        this.sendOperation = new EventHubTemplate(mockClientFactory);
        this.message = EventData.create(payload.getBytes());
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).send(isA(EventData.class));
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        verify(this.mockClientFactory, times(times)).getPartitionSenderCreator();
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getEventHubClientCreator()).thenReturn((s) -> {
            throw new EventHubRuntimeException("couldn't create the event hub client.");
        });
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getEventHubClientCreator();
    }

    @Override
    protected void verifySendWithPartitionKey(int times) {
        verify(this.mockClient, times(times)).send(isA(EventData.class), eq(partitionKey));
    }

    @Override
    protected void verifySendWithPartitionId(int times) {
        verify(this.mockSender, times(times)).send(isA(EventData.class));
    }

}
