/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.test.support.AzureMessageConverterTest;
import org.springframework.messaging.MessageHeaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceBusMessageConverterTest extends AzureMessageConverterTest<IMessage> {
    @Override
    protected IMessage getInstance() {
        return new Message(this.payload.getBytes());
    }

    @Override
    public AzureMessageConverter<IMessage> getConverter() {
        return new ServiceBusMessageConverter();
    }

    @Override
    protected Class<IMessage> getTargetClass() {
        return IMessage.class;
    }

    protected void assertMessageHeadersEqual(IMessage serviceBusMessage,
                                             org.springframework.messaging.Message<?> message) {
        assertEquals(serviceBusMessage.getMessageId(), message.getHeaders().get(AzureHeaders.RAW_ID));
        assertEquals(serviceBusMessage.getContentType(),
                message.getHeaders().get(MessageHeaders.CONTENT_TYPE, String.class));
        assertEquals(serviceBusMessage.getReplyTo(),
                message.getHeaders().get(MessageHeaders.REPLY_CHANNEL, String.class));
        assertNotNull(serviceBusMessage.getProperties().get(headerProperties));
        assertNotNull(message.getHeaders().get(headerProperties, String.class));
        assertEquals(serviceBusMessage.getProperties().get(headerProperties),
                message.getHeaders().get(headerProperties, String.class));
    }
}
