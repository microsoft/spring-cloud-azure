/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */


package com.microsoft.azure.spring.integration.storagequeue.factory;

import com.microsoft.azure.storage.queue.CloudQueue;


import java.util.function.Function;

public interface StorageQueueFactory {
    /**
     * Return a function which accepts storage queue name, then returns {@link CloudQueue}
     */
    Function<String, CloudQueue> getQueueCreator();
}
