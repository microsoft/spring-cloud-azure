/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.IEventProcessorFactory;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.eventhub.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubRuntimeException;
import com.microsoft.azure.spring.integration.eventhub.EventHubTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubTemplateTest {

    @Mock
    private EventHubClientFactory mockClientFactory;

    @Mock
    private EventHubClient mockClient;

    @Mock
    private PartitionSender mockSender;

    @Mock
    private EventProcessorHost host;

    private String eventHubName = "event-hub";
    private String consumerGroup = "consumer-group";
    private EventHubTemplate eventHubTemplate;
    private String payload = "payload";
    private EventData message = EventData.create(payload.getBytes());
    private String partitionKey = "key";
    private String partitionId = "1";

    private CompletableFuture<Void> future = new CompletableFuture<>();

    @Before
    public void setUp() {
        this.eventHubTemplate = new EventHubTemplate(this.mockClientFactory);

        when(this.mockClientFactory.getEventHubClientCreator()).thenReturn(s -> this.mockClient);
        when(this.mockClient.send(isA(EventData.class))).thenReturn(this.future);
        when(this.mockClient.send(isA(EventData.class), eq(partitionKey))).thenReturn(this.future);

        when(this.mockClientFactory.getPartitionSenderCreator()).thenReturn(t -> this.mockSender);
        when(this.mockSender.send(isA(EventData.class))).thenReturn(this.future);
        when(this.mockClientFactory.getProcessorHostCreator()).thenReturn(t -> this.host);
        when(this.host.registerEventProcessorFactory(isA(IEventProcessorFactory.class)))
                .thenReturn(new CompletableFuture<>());
    }

    @Test
    public void testSendWithoutPartitionSupplier() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        CompletableFuture<Void> future = this.eventHubTemplate.sendAsync(eventHubName, message, null);

        assertEquals(null, future.get());
        verify(this.mockClient, times(1)).send(isA(EventData.class));
    }

    @Test
    public void testSendWithoutPartition() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        CompletableFuture<Void> future =
                this.eventHubTemplate.sendAsync(eventHubName, message, new PartitionSupplier());

        assertEquals(null, future.get());
        verify(this.mockClient, times(1)).send(isA(EventData.class));
    }

    @Test
    public void testSendWithPartitionId() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionId(partitionId);
        CompletableFuture<Void> future = this.eventHubTemplate.sendAsync(eventHubName, message, partitionSupplier);

        assertEquals(null, future.get());
        verify(this.mockSender, times(1)).send(isA(EventData.class));
        verify(this.mockClientFactory, times(1)).getPartitionSenderCreator();
    }

    @Test
    public void testSendWithPartitionKey() throws ExecutionException, InterruptedException {
        this.future.complete(null);
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey(partitionKey);
        CompletableFuture<Void> future = this.eventHubTemplate.sendAsync(eventHubName, message, partitionSupplier);

        assertEquals(null, future.get());
        verify(this.mockClient, times(1)).send(isA(EventData.class), eq(partitionKey));
        verify(this.mockClientFactory, times(1)).getEventHubClientCreator();
    }

    @Test(expected = EventHubRuntimeException.class)
    public void testSendCreateSenderFailure() throws Throwable {
        when(this.mockClientFactory.getEventHubClientCreator()).thenReturn((s) -> {
            throw new EventHubRuntimeException("couldn't create the event hub client.");
        });

        try {
            this.eventHubTemplate.sendAsync(eventHubName, this.message, null).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testSendFailure() {
        CompletableFuture<Void> future = this.eventHubTemplate.sendAsync(eventHubName, this.message, null);
        this.future.completeExceptionally(new Exception("future failed."));

        try {
            future.get();
            fail("Test should fail.");
        } catch (InterruptedException ie) {
            fail("get() should fail with an ExecutionException.");
        } catch (ExecutionException ee) {
            assertEquals("future failed.", ee.getCause().getMessage());
        }
    }

    @Test
    public void testSubscribe() {
        this.eventHubTemplate.subscribe(eventHubName, this::handleMessage, consumerGroup);

        verify(this.mockClientFactory, times(1)).getProcessorHostCreator();
    }

    private void handleMessage(Iterable<EventData> events) {
    }
}
