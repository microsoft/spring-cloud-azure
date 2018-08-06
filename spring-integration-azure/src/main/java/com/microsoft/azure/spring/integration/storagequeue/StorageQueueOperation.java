/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */


package com.microsoft.azure.spring.integration.storagequeue;


import com.microsoft.azure.spring.integration.core.QueueOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public interface StorageQueueOperation extends QueueOperation<CloudQueueMessage> {
}
