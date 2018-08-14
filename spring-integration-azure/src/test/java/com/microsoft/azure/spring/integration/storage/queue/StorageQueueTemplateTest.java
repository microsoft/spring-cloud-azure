/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueTemplateTest {

    @Mock
    private StorageQueueFactory mockStorageQueueFactory;

    @Mock
    private CloudQueue mockCloudQueue;

    private StorageQueueTemplate storageQueueTemplate;
    private String destination = "test-destination";
    private int visibilityTimeoutInSeconds = 30;
    private CloudQueueMessage cloudQueueMessage = new CloudQueueMessage("test message");

    @Before
    public void setup() throws StorageException {
        this.storageQueueTemplate = new StorageQueueTemplate(mockStorageQueueFactory);
        when(this.mockStorageQueueFactory.getQueueCreator()).thenReturn(t -> mockCloudQueue);
        when(this.mockCloudQueue.retrieveMessage(eq(visibilityTimeoutInSeconds), eq(null), eq(null)))
                .thenReturn(this.cloudQueueMessage);
    }

    @Test
    public void testSendAsync() throws ExecutionException, InterruptedException, StorageException {
        CompletableFuture<Void> future = this.storageQueueTemplate.sendAsync(destination,
                cloudQueueMessage, null);
        assertNull(future.get());
        verify(this.mockCloudQueue, times(1)).addMessage(cloudQueueMessage);
    }

    @Test
    public void testReceiveAsync() throws ExecutionException, InterruptedException, StorageException {
        CompletableFuture<CloudQueueMessage> future = this.storageQueueTemplate.receiveAsync(destination);
        assertThat(future.get(), equalTo(this.cloudQueueMessage));
        verify(this.mockCloudQueue, times(1))
                .retrieveMessage(visibilityTimeoutInSeconds, null, null);
    }

}
