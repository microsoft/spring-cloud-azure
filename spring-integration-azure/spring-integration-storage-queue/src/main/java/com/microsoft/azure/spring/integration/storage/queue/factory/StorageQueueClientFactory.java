/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.factory;

import com.microsoft.azure.storage.queue.CloudQueue;

/**
 * @author Miao Cao
 */
public interface StorageQueueClientFactory {

    CloudQueue getOrCreateQueueClient(String storageAccountName, String queueName);
}
