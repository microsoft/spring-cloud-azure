/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubRuntimeException;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
import com.microsoft.azure.spring.integration.test.support.SendOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubTemplateSendTest extends SendOperationTest<EventHubOperation> {

    @Mock
    private EventHubClientFactory mockClientFactory;

    @Mock
    private EventHubClient mockClient;

    @Mock
    private PartitionSender mockSender;

    @Before
    public void setUp() {

        when(this.mockClientFactory.getOrCreateClient(this.destination)).thenReturn(this.mockClient);
        when(this.mockClient.send(anyCollection())).thenReturn(this.future);
        when(this.mockClient.send(anyCollection(), eq(partitionKey))).thenReturn(this.future);

        when(this.mockClientFactory.getOrCreatePartitionSender(eq(this.destination), anyString()))
                .thenReturn(this.mockSender);
        when(this.mockSender.send(anyCollection())).thenReturn(this.future);
        this.sendOperation = new EventHubTemplate(mockClientFactory);
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).send(anyCollection());
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        verify(this.mockClientFactory, times(times)).getOrCreatePartitionSender(eq(this.destination), anyString());
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getOrCreateClient(this.destination)).thenThrow(EventHubRuntimeException.class);
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getOrCreateClient(this.destination);
    }

    @Override
    protected void verifySendWithPartitionKey(int times) {
        verify(this.mockClient, times(times)).send(anyCollection(), eq(partitionKey));
    }

    @Override
    protected void verifySendWithPartitionId(int times) {
        verify(this.mockSender, times(times)).send(anyCollection());
    }

}
