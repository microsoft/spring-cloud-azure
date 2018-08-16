/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.inbound;

import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueMessageSourceTest {
    @Mock
    private StorageQueueOperation storageQueueOperation;
    @Mock
    private Checkpointer<CloudQueueMessage> checkpointer;

    private String destination = "test-destination";
    private CloudQueueMessage cloudQueueMessage = new CloudQueueMessage("test message");
    private StorageQueueMessageSource messageSource;
    private CompletableFuture<CloudQueueMessage> future = new CompletableFuture<>();


    @Before
    public void setup() {
        when(this.storageQueueOperation.getCheckpointer(eq(destination))).thenReturn(this.checkpointer);
        messageSource = new StorageQueueMessageSource(destination, storageQueueOperation);
    }

    @Test
    public void testDoReceiveWhenHaveNotMessage() {
        future.complete(null);
        when(this.storageQueueOperation.receiveAsync(eq(destination)))
                .thenReturn(future);
        assertNull(messageSource.doReceive());
    }

    @Test
    public void testDoReceiveWithRecordCheckpointerMode() throws StorageException {
        future.complete(this.cloudQueueMessage);
        when(this.storageQueueOperation.receiveAsync(eq(destination)))
                .thenReturn(future);
        Message<byte[]> message = (Message<byte[]>) messageSource.doReceive();
        verify(checkpointer, times(1)).checkpoint(this.cloudQueueMessage);
        assertEquals(new String(message.getPayload()), this.cloudQueueMessage.getMessageContentAsString());
    }

    @Test
    public void testDoReceiveWithManualCheckpointerMode() throws StorageException {
        future.complete(this.cloudQueueMessage);
        when(this.storageQueueOperation.receiveAsync(eq(destination)))
                .thenReturn(future);
        this.messageSource.setCheckpointMode(CheckpointMode.MANUAL);
        Message<byte[]> message = (Message<byte[]>) messageSource.doReceive();
        verify(checkpointer, times(0)).checkpoint(this.cloudQueueMessage);
        assertEquals(new String(message.getPayload()), this.cloudQueueMessage.getMessageContentAsString());
    }
}
