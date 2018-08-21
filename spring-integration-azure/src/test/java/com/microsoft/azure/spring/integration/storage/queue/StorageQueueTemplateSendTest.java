/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.SendOperationTest;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueFactory;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueTemplateSendTest extends SendOperationTest<CloudQueueMessage, StorageQueueOperation> {

    @Mock
    private StorageQueueFactory mockStorageQueueFactory;

    @Mock
    private CloudQueue mockCloudQueue;

    @Before
    public void setup() {
        when(this.mockStorageQueueFactory.getQueueCreator()).thenReturn(t -> mockCloudQueue);
        this.sendOperation = new StorageQueueTemplate(mockStorageQueueFactory);
        this.message = new CloudQueueMessage("test message");
    }

    @Override
    @Test
    public void testSendFailure() throws StorageException {
        doThrow(StorageException.class)
                .when(mockCloudQueue)
                .addMessage(isA(CloudQueueMessage.class));

        CompletableFuture<Void> future = this.sendOperation.sendAsync(this.destination, this.message, null);
        this.future.completeExceptionally(new Exception("future failed."));

        try {
            future.get();
            fail("Test should fail.");
        } catch (InterruptedException ie) {
            fail("get() should fail with an ExecutionException.");
        } catch (ExecutionException ee) {
            assertEquals(StorageQueueRuntimeException.class, ee.getCause().getClass());
        }
    }

    @Override
    protected void verifySendCalled(int times) {
        try {
            verify(this.mockCloudQueue, times(times)).addMessage(isA(CloudQueueMessage.class));
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to add message to cloud queue", e);
        }
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        //Unsupported feature
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockStorageQueueFactory.getQueueCreator()).thenReturn((s) -> {
            throw new StorageQueueRuntimeException("Failed to get queue creator.");
        });
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockStorageQueueFactory, times(times)).getQueueCreator();
    }

    @Override
    protected void verifySendWithPartitionKey(int times) {
        //Unsupported feature
    }

    @Override
    protected void verifySendWithPartitionId(int times) {
        //Unsupported feature
    }
}
