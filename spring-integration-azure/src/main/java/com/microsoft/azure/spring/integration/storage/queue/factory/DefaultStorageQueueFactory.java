/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.factory;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.AzureUtil;
import com.microsoft.azure.spring.integration.core.Memoizer;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueRuntimeException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.function.Function;

public class DefaultStorageQueueFactory implements StorageQueueFactory {

    private final AzureAdmin azureAdmin;
    private final StorageAccount storageAccount;
    private final Function<String, CloudQueue> queueCreater = Memoizer.memoize(this::createStorageQueue);
    private CloudQueueClient cloudQueueClient;

    public DefaultStorageQueueFactory(@NonNull AzureAdmin azureAdmin, String storageAccountName) {
        this.azureAdmin = azureAdmin;
        this.storageAccount = azureAdmin.getOrCreateStorageAccount(storageAccountName);
    }
    
    @Override
    public Function<String, CloudQueue> getQueueCreator() {
        return queueCreater;
    }

    private CloudQueueClient createStorageQueueClient() {
        String connectionString = AzureUtil.getConnectionString(storageAccount);
        CloudStorageAccount account;
        try {
            account = CloudStorageAccount.parse(connectionString);
        }catch (URISyntaxException | InvalidKeyException e) {
            throw new StorageQueueRuntimeException("Failed to parse connection string.", e);
        }
        return account.createCloudQueueClient();
    }

    private CloudQueue createStorageQueue(String queueName) {
        Assert.hasText(queueName, "queueName can't be null or empty");
        if(this.cloudQueueClient == null) {
            this.cloudQueueClient = createStorageQueueClient();
        }
        try {
            CloudQueue queue = this.cloudQueueClient.getQueueReference(queueName);
            queue.createIfNotExists();
            return queue;
        } catch (URISyntaxException e) {
            throw new StorageQueueRuntimeException("Failed to parse connection string.", e);
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to create cloud queue.", e);
        }
    }
}
