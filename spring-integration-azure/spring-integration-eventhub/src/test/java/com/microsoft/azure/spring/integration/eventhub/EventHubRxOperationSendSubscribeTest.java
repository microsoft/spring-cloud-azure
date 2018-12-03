/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubRxOperation;
import com.microsoft.azure.spring.integration.eventhub.support.RxEventHubTestOperation;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import com.microsoft.azure.spring.integration.test.support.rx.RxSendSubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubRxOperationSendSubscribeTest extends RxSendSubscribeByGroupOperationTest<EventHubRxOperation> {

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

        this.sendSubscribeOperation = new RxEventHubTestOperation(null, () -> context);
    }

    @Override
    protected void verifyCheckpointSuccessCalled(int times) {
        verify(this.context, times(times)).checkpoint(isA(EventData.class));
    }

    @Override
    protected void verifyCheckpointBatchSuccessCalled(int times) {
        verify(this.context, times(times)).checkpoint();
    }

    @Test
    public void testSendReceiveWithBatchCheckpointMode() {
        sendSubscribeOperation
                .setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.BATCH).build());
        sendSubscribeOperation.setStartPosition(StartPosition.EARLIEST);
        Arrays.stream(messages).forEach(m -> sendSubscribeOperation.sendRx(destination, m));
        sendSubscribeOperation.subscribe(destination, consumerGroup, User.class).test()
                              .assertValueCount(messages.length).assertNoErrors();
        verifyCheckpointBatchSuccessCalled(1);
    }

    @Test
    public void testHasPartitionIdHeader() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, User.class);
        sendSubscribeOperation.sendRx(destination, userMessage);
    }

}
