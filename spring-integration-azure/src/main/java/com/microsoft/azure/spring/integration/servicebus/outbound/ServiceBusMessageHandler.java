/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.outbound;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.AbstractAzureMessageHandler;
import com.microsoft.azure.spring.integration.core.SendOperation;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Outbound channel adapter to publish messages to Azure Service Bus topic and queue.
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageHandler extends AbstractAzureMessageHandler<IMessage> {

    public ServiceBusMessageHandler(String destination, SendOperation<IMessage> sendOperation) {
        super(destination, sendOperation);
    }

    @Override
    public IMessage toAzureMessage(Message<?> message) {
        com.microsoft.azure.servicebus.Message serviceBusMessage;

        Object payload = message.getPayload();
        if (payload instanceof com.microsoft.azure.servicebus.Message) {
            serviceBusMessage = (com.microsoft.azure.servicebus.Message) payload;
        } else if (payload instanceof String) {
            serviceBusMessage =
                    new com.microsoft.azure.servicebus.Message(((String) payload).getBytes(Charset.defaultCharset()));
        } else if (payload instanceof byte[]) {
            serviceBusMessage = new com.microsoft.azure.servicebus.Message((byte[]) payload);
        } else {
            serviceBusMessage = new com.microsoft.azure.servicebus.Message(String.valueOf(payload));
        }

        if (message.getHeaders().containsKey(MessageHeaders.CONTENT_TYPE)) {
            serviceBusMessage.setContentType(message.getHeaders().get(MessageHeaders.CONTENT_TYPE, String.class));
        }

        if (message.getHeaders().containsKey(MessageHeaders.ID)) {
            serviceBusMessage.setMessageId(message.getHeaders().get(MessageHeaders.ID, UUID.class).toString());
        }

        return serviceBusMessage;
    }

}
