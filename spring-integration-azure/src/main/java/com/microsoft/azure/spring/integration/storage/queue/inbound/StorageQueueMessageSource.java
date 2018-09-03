/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.inbound;

import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueRuntimeException;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import java.util.concurrent.ExecutionException;

/**
 * Inbound Message Source to receive messages from Azure Storage Queue.
 *
 * @author Miao Cao
 */
public class StorageQueueMessageSource extends AbstractMessageSource<Message<?>> {

    private StorageQueueOperation storageQueueOperation;
    private String destination;

    public StorageQueueMessageSource(String destination, StorageQueueOperation storageQueueOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.storageQueueOperation = storageQueueOperation;
        this.destination = destination;
    }

    @Override
    protected Object doReceive() {
        Message<?> message;
        try {
            message = storageQueueOperation.receiveAsync(destination).get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new StorageQueueRuntimeException("Failed to receive message.", e);
        }

        return message;
    }

    @Override
    public String getComponentType() {
        return "storage-queue:message-source";
    }
}
