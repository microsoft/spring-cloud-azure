/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue.outbound;

import com.microsoft.azure.spring.integration.core.AbstractAzureMessageHandler;
import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.springframework.messaging.Message;

public class StorageQueueMessageHandler extends AbstractAzureMessageHandler<CloudQueueMessage> {

    public StorageQueueMessageHandler(String destination, SendOperation<CloudQueueMessage> sendOperation) {
        super(destination, sendOperation);
    }

    @Override
    public CloudQueueMessage toAzureMessage(Message<?> message) {
        Object payload  = message.getPayload();
        if (payload instanceof CloudQueueMessage) {
            return (CloudQueueMessage) payload;
        }
        if (payload instanceof String) {
            return new CloudQueueMessage((String) payload);
        }
        if (payload instanceof byte[]) {
            return new CloudQueueMessage((byte[]) payload);
        }

        return new CloudQueueMessage((byte[]) this.messageConverter.fromMessage(message, byte[].class));
    }
}
