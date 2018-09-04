/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.converter;

import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverterTest;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class StorageQueueMessageConverterTest extends AzureMessageConverterTest<CloudQueueMessage> {
    @Override
    protected CloudQueueMessage getInstance() {
        return new CloudQueueMessage(this.payload.getBytes());
    }

    @Override
    protected AzureMessageConverter<CloudQueueMessage> getConverter() {
        return new StorageQueueMessageConverter();
    }

    @Override
    protected Class<CloudQueueMessage> getTargetClass() {
        return CloudQueueMessage.class;
    }

    @Override
    protected <U> void assertMessagePayloadEquals(U convertedPayload, U payload){
        if (convertedPayload.getClass().equals(byte[].class)) {
            assertTrue(Arrays.equals((byte[]) convertedPayload, (byte[]) payload));
        } else {
            super.assertMessagePayloadEquals(convertedPayload, payload);
        }
    }
}
