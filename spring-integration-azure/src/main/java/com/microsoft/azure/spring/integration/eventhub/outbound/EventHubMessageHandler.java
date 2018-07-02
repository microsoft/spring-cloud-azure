/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.outbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.AbstractAzureMessageHandler;
import com.microsoft.azure.spring.integration.core.SendOperation;
import org.springframework.messaging.Message;

import java.nio.charset.Charset;

/**
 * Outbound channel adapter to publish messages to Azure Event Hub.
 *
 * @author Warren Zhu
 */
public class EventHubMessageHandler extends AbstractAzureMessageHandler<EventData> {

    public EventHubMessageHandler(String destination, SendOperation<EventData> sendOperation) {
        super(destination, sendOperation);
    }

    @Override
    public EventData toAzureMessage(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof EventData) {
            return (EventData) payload;
        }

        if (payload instanceof String) {
            return EventData.create(((String) payload).getBytes(Charset.defaultCharset()));
        }

        if (payload instanceof byte[]) {
            return EventData.create((byte[]) payload);
        }

        return EventData.create((byte[]) this.messageConverter.fromMessage(message, byte[].class));
    }

}
