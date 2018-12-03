/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.microsoft.azure.spring.integration.test.support.SendSubscribeByGroupOperationTest;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubOperationSendSubscribeTest extends SendSubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    PartitionContext context;

    @Before
    @Override
    public void setUp() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.context.getPartitionId()).thenReturn("1");
        when(this.context.checkpoint()).thenReturn(future);
        when(this.context.checkpoint(isA(EventData.class))).thenReturn(future);

        this.sendSubscribeOperation = new EventHubTestOperation(null, () -> context);
    }

    @Override
    protected void verifyCheckpointSuccessCalled(int times) {
        verify(this.context, times(times)).checkpoint(isA(EventData.class));
    }

    @Override
    protected void verifyCheckpointBatchSuccessCalled(int times) {
        verify(this.context, times(times)).checkpoint();
    }

    @Override
    protected void verifyCheckpointFailureCalled(int times) {

    }

    @Test
    public void testSendReceiveWithBatchCheckpointMode() {
        sendSubscribeOperation
                .setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.BATCH).build());
        sendSubscribeOperation.setStartPosition(StartPosition.EARLIEST);
        messages.forEach(m -> sendSubscribeOperation.sendAsync(destination, m));
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::batchCheckpointHandler, User.class);
        verifyCheckpointBatchSuccessCalled(1);
    }

    private void batchCheckpointHandler(Message<?> message) {
    }

    @Test
    public void testHasPartitionIdHeader() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::partitionIdHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    private void partitionIdHandler(Message<?> message) {
        assertTrue(message.getHeaders().containsKey(AzureHeaders.PARTITION_ID));
        String partitionId = message.getHeaders().get(AzureHeaders.PARTITION_ID, String.class);
        assertNotNull(partitionId);
        assertEquals(this.partitionId, partitionId);
    }

    @Override
    protected void verifyCheckpointFailure(Checkpointer checkpointer) {
    }
}
