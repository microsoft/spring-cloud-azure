/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.inbound;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.UUID;

public class ServiceBusTopicInboundChannelAdapter extends AbstractInboundChannelAdapter<IMessage, UUID> {

    public ServiceBusTopicInboundChannelAdapter(String destination,
            @NonNull SubscribeByGroupOperation<IMessage, UUID> subscribeByGroupOperation, String consumerGroup) {
        super(destination);
        Assert.hasText(consumerGroup, "consumerGroup cannot be null or empty");
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        this.consumerGroup = consumerGroup;
    }

    @Override
    protected Object getPayload(IMessage data) {
        return data.getBody();
    }

    @Override
    protected UUID getCheckpointKey(IMessage data) {
        return data.getLockToken();
    }
}
