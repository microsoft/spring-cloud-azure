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
import org.springframework.util.Assert;


public class StorageQueueTemplate implements StorageQueueOperation {
    private final StorageQueueFactory storageQueueFactory;
    private final CloudQueue cloudQueue;

    StorageQueueTemplate(StorageQueueFactory storageQueueFactory, String queueName) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        this.storageQueueFactory = storageQueueFactory;
        this.cloudQueue = storageQueueFactory.getQueueCreator().apply(queueName);
    }

    @Override
    public boolean add(CloudQueueMessage cloudQueueMessage) {
        try {
            cloudQueue.addMessage(cloudQueueMessage);
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to add message to cloud queue", e);
        }
        return true;
    }

    @Override
    public CloudQueueMessage peek() {
        try {
            return cloudQueue.peekMessage();
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to peek message from cloud queue", e);
        }
    }

    @Override
    public CloudQueueMessage retrieve() {
        try {
            return cloudQueue.retrieveMessage();
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to retrieve message from cloud queue", e);
        }
    }

    @Override
    public boolean delete(CloudQueueMessage cloudQueueMessage) {
        try {
            cloudQueue.deleteMessage(cloudQueueMessage);
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to delete message from cloud queue", e);
        }
        return true;
    }

}
