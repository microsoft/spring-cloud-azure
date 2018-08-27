/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import org.springframework.util.Assert;

public class EventHubInboundChannelAdapter extends AbstractInboundChannelAdapter {

    public EventHubInboundChannelAdapter(String destination, SubscribeByGroupOperation subscribeByGroupOperation,
            String consumerGroup) {
        super(destination);
        Assert.hasText(consumerGroup, "consumerGroup can't be null or empty");
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        this.consumerGroup = consumerGroup;
    }
}
