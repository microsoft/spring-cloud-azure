/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.converter;

import com.microsoft.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import com.microsoft.azure.spring.integration.core.converter.ConversionException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class StorageQueueMessageConverter extends AbstractAzureMessageConverter<CloudQueueMessage> {
    @Override
    protected byte[] getPayload(CloudQueueMessage azureMessage) {
        try {
            return azureMessage.getMessageContentAsByte();
        } catch (StorageException e) {
            throw new ConversionException("Failed to get queue message content", e);
        }
    }

    @Override
    protected CloudQueueMessage fromString(String payload) {
        return new CloudQueueMessage(payload);
    }

    @Override
    protected CloudQueueMessage fromByte(byte[] payload) {
        return new CloudQueueMessage(payload);
    }

}
