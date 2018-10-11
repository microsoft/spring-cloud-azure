/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueTemplateReceiveTest {

    @Mock
    private StorageQueueClientFactory mockClientFactory;

    @Mock
    private CloudQueue mockClient;

    private StorageQueueOperation operation;

    private CloudQueueMessage cloudQueueMessage = new CloudQueueMessage("test message");
    private int visibilityTimeoutInSeconds = 30;
    private String destination = "queue";


    @Before
    public void setup() throws StorageException {
        when(this.mockClientFactory.getOrCreateQueue(any(), eq(destination))).thenReturn(this.mockClient);
        when(this.mockClient.retrieveMessage(anyInt(), eq(null), eq(null))).thenReturn(this.cloudQueueMessage);
        this.operation = new StorageQueueTemplate(this.mockClientFactory, "");
    }

    @Test
    public void testReceiveFailure() throws StorageException {
        when(this.mockClient.retrieveMessage(eq(visibilityTimeoutInSeconds), eq(null), eq(null)))
                .thenThrow(StorageException.class);

        CompletableFuture<Message<?>> future =
                this.operation.receiveAsync(this.destination, this.visibilityTimeoutInSeconds);
        verifyStorageQueueRuntimeExceptionThrown(future);
    }

    @Test
    public void testReceiveSuccessWithRecordMode() {
        CompletableFuture<Message<?>> future = this.operation.receiveAsync(destination);
        try {
            assertTrue(Arrays.equals((byte[]) future.get().getPayload(),
                    this.cloudQueueMessage.getMessageContentAsByte()));
        } catch (InterruptedException | ExecutionException | StorageException e) {
            fail("Test should not throw Exception.");
        }
        try {
            verify(this.mockClient, times(1)).retrieveMessage(visibilityTimeoutInSeconds, null, null);
        } catch (StorageException e) {
            fail("Test should not throw StorageException.");
        }

        try {
            Map<String, Object> headers = future.get().getHeaders();
            assertNull(headers.get(AzureHeaders.CHECKPOINTER));
        } catch (InterruptedException | ExecutionException e) {
            fail("Test should not throw Exception.");
        }
    }

    private void verifyStorageQueueRuntimeExceptionThrown(CompletableFuture<Message<?>> future) {
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
    public void testReceiveSuccessWithManualMode() throws StorageException {
        operation.setCheckpointMode(CheckpointMode.MANUAL);
        CompletableFuture<Message<?>> future = this.operation.receiveAsync(destination);

        try {
            Map<String, Object> headers = future.get().getHeaders();
            Checkpointer checkpointer = (Checkpointer) headers.get(AzureHeaders.CHECKPOINTER);
            CompletableFuture<Void> checkpointFuture = checkpointer.success();
            checkpointFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            fail("Test should not throw Exception.");
        }

        verify(this.mockClient, times(1)).deleteMessage(cloudQueueMessage);
    }
}
