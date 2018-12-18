/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.factory;

import com.microsoft.azure.spring.cloud.context.core.api.ResourceManager;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.net.URISyntaxException;
import java.util.function.Function;

public class DefaultStorageQueueClientFactory implements StorageQueueClientFactory {
    private static final Logger log = LoggerFactory.getLogger(DefaultStorageQueueClientFactory.class);
    private ResourceManager<CloudQueue, Tuple<CloudStorageAccount, String>> storageQueueManager;
    private final Function<String, CloudQueue> cloudQueueCreator = Memoizer.memoize(this::createCloudQueue);
    private final CloudStorageAccount cloudStorageAccount;

    public DefaultStorageQueueClientFactory(
            @NonNull CloudStorageAccount cloudStorageAccount) {
        this.cloudStorageAccount = cloudStorageAccount;
    }

    @Override
    public CloudQueue getOrCreateQueueClient(String queueName) {
        return this.cloudQueueCreator.apply(queueName);
    }

    private CloudQueue createCloudQueue(String queueName){
        if(storageQueueManager != null) {
            storageQueueManager.getOrCreate(Tuple.of(this.cloudStorageAccount, queueName));
        }

        try {
            return cloudStorageAccount.createCloudQueueClient().getQueueReference(queueName);
        } catch (URISyntaxException | StorageException e) {
            String message = String.format("Failed to create cloud queue '%s'", queueName);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public void setStorageQueueManager(
            ResourceManager<CloudQueue, Tuple<CloudStorageAccount, String>> storageQueueManager) {
        this.storageQueueManager = storageQueueManager;
    }
}
