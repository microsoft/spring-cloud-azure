/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.SendOperationTest;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueTemplateSendTest extends SendOperationTest<StorageQueueOperation> {

    @Mock
    private StorageQueueClientFactory mockClientFactory;

    @Mock
    private CloudQueue mockClient;

    @Before
    public void setup() {
        when(this.mockClientFactory.getQueueCreator()).thenReturn(t -> mockClient);
        this.sendOperation = new StorageQueueTemplate(mockClientFactory);
    }

    @Override
    @Test
    public void testSendFailure() {
        try {
            doThrow(StorageException.class)
                    .when(mockClient)
                    .addMessage(isA(CloudQueueMessage.class));
        } catch (StorageException e) {
            // StorageException is never thrown here
        }

        CompletableFuture<Void> future = this.sendOperation.sendAsync(this.destination, this.message, null);

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
            verify(this.mockClient, times(times)).addMessage(isA(CloudQueueMessage.class));
        } catch (StorageException e) {
            // StorageException is never thrown here
        }
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        // Unsupported feature
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getQueueCreator()).thenReturn((s) -> {
            throw new StorageQueueRuntimeException("Failed to get queue creator.");
        });
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getQueueCreator();
    }

    @Override
    protected void verifySendWithPartitionKey(int times) {
        // Unsupported feature
    }

    @Override
    protected void verifySendWithPartitionId(int times) {
        // Unsupported feature
    }

    @Override
    @Test
    public void testSendWithPartitionKey() {
        // Unsupported feature
    }

    @Override
    @Test
    public void testSendWithPartitionId() {
        // Unsupported feature
    }
}
