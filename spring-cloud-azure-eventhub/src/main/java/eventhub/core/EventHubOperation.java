/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.microsoft.azure.eventhubs.EventData;
import eventhub.integration.inbound.Subscriber;
import eventhub.integration.outbound.PartitionSupplier;

import java.util.concurrent.CompletableFuture;

/**
 * Azure event hub operation to support send data asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface EventHubOperation {

    CompletableFuture<Void> sendAsync(String eventHubName, EventData eventData, PartitionSupplier partitionSupplier);

    /**
     * Return {@link Subscriber<EventData>} which could be used to receive and process {@link EventData}
     */
    Subscriber<EventData> subscribe(String eventHubName, String consumerGroup);
}
