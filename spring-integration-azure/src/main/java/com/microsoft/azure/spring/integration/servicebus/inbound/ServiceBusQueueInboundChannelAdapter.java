/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.inbound;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.SubscribeOperation;
import org.springframework.lang.NonNull;

import java.util.UUID;

public class ServiceBusQueueInboundChannelAdapter extends AbstractInboundChannelAdapter<IMessage, UUID> {

    public ServiceBusQueueInboundChannelAdapter(String destination,
            @NonNull SubscribeOperation<IMessage, UUID> subscribeOperation) {
        super(destination);
        this.subscribeOperation = subscribeOperation;
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
