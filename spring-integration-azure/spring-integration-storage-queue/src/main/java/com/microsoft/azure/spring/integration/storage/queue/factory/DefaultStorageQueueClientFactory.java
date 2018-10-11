/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.factory;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManager;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
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

public class DefaultStorageQueueClientFactory implements StorageQueueClientFactory {
    private final ResourceManager<StorageAccount, String> storageAccountManager;
    private final Function<String, CloudQueueClient> queueClientCreator =
            Memoizer.memoize(this::createStorageQueueClient);
    private final Function<Tuple<String, String>, CloudQueue> queueCreator = Memoizer.memoize(this::createStorageQueue);

    public DefaultStorageQueueClientFactory(@NonNull ResourceManager<StorageAccount, String> storageAccountManager) {
        this.storageAccountManager = storageAccountManager;
    }

    private CloudQueueClient createStorageQueueClient(String storageAccountName) {
        StorageAccount storageAccount = storageAccountManager.getOrCreate(storageAccountName);
        String connectionString = StorageConnectionStringProvider.getConnectionString(storageAccount);

        try {
            CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            return account.createCloudQueueClient();
        } catch (URISyntaxException | InvalidKeyException e) {
            throw new StorageQueueRuntimeException("Failed to build queue client", e);
        }
    }

    private CloudQueue createStorageQueue(Tuple<String, String> storageAccountAndQueue) {
        String storageAccountName = storageAccountAndQueue.getFirst();
        String queueName = storageAccountAndQueue.getSecond();
        Assert.hasText(queueName, "queueName can't be null or empty");
        try {
            CloudQueue queue = this.queueClientCreator.apply(storageAccountName).getQueueReference(queueName);
            queue.createIfNotExists();
            return queue;
        } catch (StorageException | URISyntaxException e) {
            // StorageException should be never thrown.
            throw new StorageQueueRuntimeException("Failed to create queue" + queueName, e);
        }
    }

    @Override
    public CloudQueueClient getOrCreateQueueClient(String storageAccountName) {
        return queueClientCreator.apply(storageAccountName);
    }

    @Override
    public CloudQueue getOrCreateQueue(String storageAccountName, String queueName) {
        return queueCreator.apply(Tuple.of(storageAccountName, queueName));
    }
}
