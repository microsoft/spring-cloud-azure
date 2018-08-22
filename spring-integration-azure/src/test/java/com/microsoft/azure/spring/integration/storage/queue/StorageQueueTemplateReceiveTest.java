/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueTemplateReceiveTest {

    @Mock
    private StorageQueueClientFactory mockStorageQueueClientFactory;

    @Mock
    private CloudQueue mockCloudQueue;

    private StorageQueueOperation operation;

    private CloudQueueMessage cloudQueueMessage = new CloudQueueMessage("test message");
    private int visibilityTimeoutInSeconds = 30;
    private String destination = "queue";

    @Before
    public void setup() throws StorageException {
        this.operation = new StorageQueueTemplate(this.mockStorageQueueClientFactory);
        when(this.mockStorageQueueClientFactory.getQueueCreator()).thenReturn(t -> this.mockCloudQueue);
        when(this.mockCloudQueue.retrieveMessage(eq(this.visibilityTimeoutInSeconds), eq(null), eq(null)))
                .thenReturn(this.cloudQueueMessage);
    }

    @Test
    public void testReceiveFailure() throws StorageException {
        when(this.mockCloudQueue.retrieveMessage(eq(visibilityTimeoutInSeconds), eq(null), eq(null)))
                .thenThrow(StorageException.class);

        CompletableFuture<CloudQueueMessage> future =
                this.operation.receiveAsync(this.destination, this.visibilityTimeoutInSeconds);

        try {
            future.get();
            fail("Test should fail.");
        } catch (InterruptedException ie) {
            fail("get() should fail with an ExecutionException.");
        } catch (ExecutionException ee) {
            assertEquals(StorageQueueRuntimeException.class, ee.getCause().getClass());
        }
    }

    @Test
    public void testReceiveSuccess() throws ExecutionException, InterruptedException, StorageException {
        CompletableFuture<CloudQueueMessage> future = this.operation.receiveAsync(destination);
        assertEquals(future.get(), this.cloudQueueMessage);
        verify(this.mockCloudQueue, times(1))
                .retrieveMessage(visibilityTimeoutInSeconds, null, null);
    }
}
