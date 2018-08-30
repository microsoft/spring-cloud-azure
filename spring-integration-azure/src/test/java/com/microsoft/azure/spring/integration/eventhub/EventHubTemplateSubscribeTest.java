/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.EventProcessorOptions;
import com.microsoft.azure.eventprocessorhost.IEventProcessorFactory;
import com.microsoft.azure.spring.integration.SubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubTemplateSubscribeTest extends SubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    private EventHubClientFactory mockClientFactory;

    @Mock
    private EventProcessorHost host;

    @Before
    public void setUp() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        this.subscribeByGroupOperation = new EventHubTemplate(mockClientFactory);
        when(this.mockClientFactory.getProcessorHostCreator()).thenReturn(t -> this.host);
        when(this.host
                .registerEventProcessorFactory(isA(IEventProcessorFactory.class), isA(EventProcessorOptions.class)))
                .thenReturn(future);
        when(this.host.unregisterEventProcessor()).thenReturn(future);
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).getProcessorHostCreator();
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).getProcessorHostCreator();
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        verify(this.host, times(times))
                .registerEventProcessorFactory(isA(IEventProcessorFactory.class), isA(EventProcessorOptions.class));
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
        verify(this.host, times(times)).unregisterEventProcessor();
    }

}
