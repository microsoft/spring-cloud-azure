/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import org.springframework.util.Assert;

public class EventHubInboundChannelAdapter extends AbstractInboundChannelAdapter<EventData, EventData> {

    public EventHubInboundChannelAdapter(String destination,
            SubscribeByGroupOperation<EventData, EventData> subscribeByGroupOperation, String consumerGroup) {
        super(destination);
        Assert.hasText(consumerGroup, "consumerGroup can't be null or empty");
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        this.consumerGroup = consumerGroup;
    }

    @Override
    protected Object getPayload(EventData data) {
        return data.getBytes();
    }

    @Override
    protected EventData getCheckpointKey(EventData data) {
        return data;
    }
}
