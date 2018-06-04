/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;

public class EventHubCheckpointer implements Checkpointer<EventData> {
    private final PartitionContext partitionContext;

    public EventHubCheckpointer(PartitionContext partitionContext) {
        Assert.notNull(partitionContext, "partitionContext can't be null");
        this.partitionContext = partitionContext;
    }

    @Override
    public CompletableFuture<Void> checkpoint() {
        return partitionContext.checkpoint();
    }

    @Override
    public CompletableFuture<Void> checkpoint(EventData eventData) {
        return partitionContext.checkpoint(eventData);
    }
}
