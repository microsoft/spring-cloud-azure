/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.util;

import com.microsoft.azure.spring.integration.storage.queue.StorageQueueRuntimeException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public class StorageQueueHelper {
    public static String toString(CloudQueueMessage cloudQueueMessage){
        Map<String, Object> map = new LinkedHashMap<>();

        try {
            map.put("body", cloudQueueMessage.getMessageContentAsString());
        } catch (StorageException e) {
            throw new StorageQueueRuntimeException("Failed to get storage queue message content", e);
        }
        map.put("dequeueCount", cloudQueueMessage.getDequeueCount());
        map.put("id", cloudQueueMessage.getId());

        return map.toString();
    }
}
