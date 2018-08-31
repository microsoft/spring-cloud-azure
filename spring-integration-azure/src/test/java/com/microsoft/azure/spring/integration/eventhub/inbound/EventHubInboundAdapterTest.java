/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.InboundChannelAdapterTest;
import com.microsoft.azure.spring.integration.core.support.EventHubTestOperation;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

@RunWith(MockitoJUnitRunner.class)
public class EventHubInboundAdapterTest extends InboundChannelAdapterTest<EventHubInboundChannelAdapter> {

    @Mock
    PartitionContext context;

    @Override
    public void setUp() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        this.adapter = new EventHubInboundChannelAdapter(destination, new EventHubTestOperation(null, () -> context),
                consumerGroup);
    }
}
