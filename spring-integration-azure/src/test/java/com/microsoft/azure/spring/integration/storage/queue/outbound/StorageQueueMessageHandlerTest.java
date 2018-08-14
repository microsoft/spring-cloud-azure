/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.outbound;

import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueMessageHandlerTest {
    @Mock
    private StorageQueueOperation mockStorageQueueOperation;

    private String destination = "test-destination";
    private StorageQueueMessageHandler messageHandler;
    private String payload = "payload";
    private Message<?> message = new GenericMessage<>(payload);

    @Before
    public void setup() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.mockStorageQueueOperation.sendAsync(eq(destination),
                isA(CloudQueueMessage.class), isA(PartitionSupplier.class)))
                .thenReturn(future);
        messageHandler = new StorageQueueMessageHandler(destination, mockStorageQueueOperation);
    }

    @Test
    public void testHandleMessage() {
        this.messageHandler.handleMessage(this.message);
        verify(this.mockStorageQueueOperation, times(1)).sendAsync(eq(destination),
                isA(CloudQueueMessage.class), isA(PartitionSupplier.class));
    }
}
