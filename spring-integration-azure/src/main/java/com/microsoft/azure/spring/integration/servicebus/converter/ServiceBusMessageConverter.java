/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.converter;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.springframework.messaging.MessageHeaders;

import java.util.UUID;

/**
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link IMessage}
 * and vice versa.
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageConverter extends AbstractAzureMessageConverter<IMessage> {

    @Override
    protected byte[] getPayload(IMessage azureMessage) {
        return azureMessage.getBody();
    }

    @Override
    protected IMessage fromString(String payload) {
        return new Message(payload);
    }

    @Override
    protected IMessage fromByte(byte[] payload) {
        return new Message(payload);
    }

    @Override
    protected void setCustomHeaders(org.springframework.messaging.Message<?> message, IMessage serviceBusMessage) {
        //TODO: figure out how to set content-type header since value could be String or MimeType

        if (message.getHeaders().containsKey(MessageHeaders.ID)) {
            serviceBusMessage.setMessageId(message.getHeaders().get(MessageHeaders.ID, UUID.class).toString());
        }
    }
}
