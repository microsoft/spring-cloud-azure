/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */


package com.microsoft.azure.spring.integration.storagequeue.factory;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.AzureUtil;
import com.microsoft.azure.spring.integration.core.Memoizer;
import com.microsoft.azure.spring.integration.storagequeue.StorageQueueRuntimeException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.function.Function;

public class DefaultStorageQueueFactory implements StorageQueueFactory {

    private final AzureAdmin azureAdmin;
    private final StorageAccount storageAccount;

    public DefaultStorageQueueFactory(AzureAdmin azureAdmin, String storageAccountName) {
        this.azureAdmin = azureAdmin;
        this.storageAccount = azureAdmin.getOrCreateStorageAccount(storageAccountName);
    }

    private final Function<String, CloudQueue> queueCreater = Memoizer.memoize(this::createStorageQueue);
    @Override
    public Function<String, CloudQueue> getQueueCreator() {
        return queueCreater;
    }

    private CloudQueue createStorageQueue(String queueName) {
        String connectionString = AzureUtil.getConnectionString(storageAccount);
        try {
            CloudQueueClient queueClient = CloudStorageAccount.parse(connectionString).createCloudQueueClient();
            CloudQueue queue = queueClient.getQueueReference(queueName);
            queue.createIfNotExists();
            return queue;
        } catch (URISyntaxException | InvalidKeyException e) {
            throw new StorageQueueRuntimeException("Failed to parse connection string.");
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to create cloud queue.");
        }
    }
}
