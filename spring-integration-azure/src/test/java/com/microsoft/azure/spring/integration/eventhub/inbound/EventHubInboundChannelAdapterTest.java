/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubInboundChannelAdapterTest {

    private final String[] payloads = {"payload1", "payload2"};
    private final String eventHubName = "eventHub";
    private final String consumerGroup = "group";
    @Mock
    private EventHubOperation eventHubOperation;
    @Mock
    private Checkpointer<EventData> checkpointer;
    private EventHubInboundChannelAdapter adapter;
    private List<EventData> eventData =
            Arrays.stream(payloads).map(p -> EventData.create(p.getBytes())).collect(Collectors.toList());

    @Before
    public void setUp() {
        this.adapter = new EventHubInboundChannelAdapter(eventHubName, eventHubOperation, consumerGroup);
        when(eventHubOperation.getCheckpointer(eq(eventHubName), eq(consumerGroup))).thenReturn(checkpointer);
    }

    @Test
    public void testCheckpointBatchMode() throws InterruptedException {
        sendAndRecieve(CheckpointMode.BATCH);
        verify(this.checkpointer, times(1)).checkpoint();
    }

    @Test
    public void testCheckpointRecordMode() throws InterruptedException {
        sendAndRecieve(CheckpointMode.RECORD);
        verify(this.checkpointer, times(eventData.size())).checkpoint(isA(EventData.class));
    }

    private void sendAndRecieve(CheckpointMode checkpointMode) throws InterruptedException {
        DirectChannel channel = new DirectChannel();
        channel.setBeanName("output");
        this.adapter.setCheckpointMode(checkpointMode);
        this.adapter.doStart();
        this.adapter.setOutputChannel(channel);

        final CountDownLatch latch = new CountDownLatch(eventData.size());
        final List<String> receivedMessages = new CopyOnWriteArrayList<>();
        channel.subscribe(message -> {
            try {
                receivedMessages.add(new String((byte[]) message.getPayload()));
            } finally {
                latch.countDown();
            }

        });

        this.adapter.receiveMessage(eventData);
        Assert.isTrue(latch.await(5L, TimeUnit.SECONDS), "Failed to receive message");

        for (int i = 0; i < receivedMessages.size(); i++) {
            assertEquals(receivedMessages.get(i), payloads[i]);
        }
    }
}

