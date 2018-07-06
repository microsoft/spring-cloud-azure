/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.inbound;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class ServiceBusTopicInboundChannelAdapter extends AbstractInboundChannelAdapter<IMessage> {

    public ServiceBusTopicInboundChannelAdapter(String destination,
            SubscribeByGroupOperation<IMessage> subscribeByGroupOperation, String consumerGroup) {
        super(destination, subscribeByGroupOperation, consumerGroup);
    }

    @Override
    public Message<?> toMessage(IMessage message) {
        Object payload = message.getBody();
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }
        return this.messageConverter.toMessage(payload, new MessageHeaders(commonHeaders));
    }

    @Override
    protected Message<?> toMessage(Iterable<IMessage> data) {
        throw new UnsupportedOperationException();
    }
}
