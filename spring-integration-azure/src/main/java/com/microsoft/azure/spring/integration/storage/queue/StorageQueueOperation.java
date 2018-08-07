/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.QueueOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

/**
 * Operations for adding or receiving message on destination Azure storage queue.
 *
 * @author Miao Cao
 */
public interface StorageQueueOperation extends QueueOperation<CloudQueueMessage> {
}
