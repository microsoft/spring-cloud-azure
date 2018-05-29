/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.google.common.base.Strings;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import eventhub.integration.inbound.EventHubSubscriber;
import eventhub.integration.inbound.Subscriber;
import eventhub.integration.outbound.PartitionSupplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of {@link EventHubOperation}.
 *
 * <p>
 * The main integration component for sending to and consuming from event hub
 *
 * @author Warren Zhu
 */
@Component
public class EventHubTemplate implements EventHubOperation {

    private static final Log LOGGER = LogFactory.getLog(EventHubTemplate.class);

    @Autowired
    private EventHubClientFactory clientFactory;

    @Override
    public CompletableFuture<Void> sendAsync(String eventHubName, EventData eventData,
            PartitionSupplier partitionSupplier) {
        try {
            EventHubClient client = this.clientFactory.getOrCreateEventHubClient(eventHubName);

            if (partitionSupplier == null) {
                return client.send(eventData);
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionId())) {
                return this.clientFactory.getOrCreatePartitionSender(eventHubName, partitionSupplier.getPartitionId())
                                         .send(eventData);
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionKey())) {
                return client.send(eventData, partitionSupplier.getPartitionKey());
            } else {
                return client.send(eventData);
            }
        } catch (EventHubRuntimeException e) {
            LOGGER.error(String.format("Failed to send to '%s' ", eventHubName), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public Subscriber<EventData> subscribe(String eventHubName, String consumerGroup) {
        EventProcessorHost host = this.clientFactory.getOrCreateEventProcessorHost(eventHubName, consumerGroup);

        return new EventHubSubscriber(host);
    }

}
