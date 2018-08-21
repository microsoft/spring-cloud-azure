/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.outbound;

import com.microsoft.azure.spring.integration.MessageHandlerTest;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueMessageHandlerTest extends MessageHandlerTest<CloudQueueMessage, StorageQueueOperation> {

    @Before
    @Override
    public void setUp() {
        this.messageClass = CloudQueueMessage.class;
        this.future.complete(null);
        this.sendOperation = mock(StorageQueueOperation.class);
        when(this.sendOperation.sendAsync(
                eq(this.destination), isA(CloudQueueMessage.class), isA(PartitionSupplier.class)
        )).thenReturn(future);
        this.handler = new StorageQueueMessageHandler(this.destination, this.sendOperation);
    }
}
