/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class EventHubInboundChannelAdapter extends AbstractInboundChannelAdapter<EventData> {

    public EventHubInboundChannelAdapter(String destination,
            SubscribeByGroupOperation<EventData> subscribeByGroupOperation, String consumerGroup) {
        super(destination, subscribeByGroupOperation, consumerGroup);
    }

    @Override
    public Message<?> toMessage(EventData eventData) {
        Object payload = eventData.getBytes();
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }
        return this.messageConverter.toMessage(payload, new MessageHeaders(commonHeaders));
    }

    @Override
    protected Message<?> toMessage(Iterable<EventData> data) {
        throw new UnsupportedOperationException();
    }


}
