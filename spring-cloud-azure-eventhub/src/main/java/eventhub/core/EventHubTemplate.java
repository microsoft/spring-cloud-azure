/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.google.common.base.Strings;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of {@link EventHubOperation}.
 *
 * <p>
 * The main integration component for sending to and consuming from event hub
 *
 * @author Warren Zhu
 */
public class EventHubTemplate implements EventHubOperation {

    private static final Log LOGGER = LogFactory.getLog(EventHubTemplate.class);

    private final EventHubClientFactory clientFactory;

    public EventHubTemplate(EventHubClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void send(String eventHubName, EventData eventData, PartitionSupplier partitionSupplier) {
        try {
            sendAsync(eventHubName, eventData, partitionSupplier).get();
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted", e);
            throw new EventHubRuntimeException("Thread interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.warn("Failed to send event hub data to " + eventHubName, e);
            throw new EventHubRuntimeException("Failed to send event hub data to " + eventHubName, e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(String eventHubName, EventData eventData,
                                             PartitionSupplier partitionSupplier) {
        EventHubClient client = this.clientFactory.createEventHubClient(eventHubName);

        if (client == null) {
            throw new EventHubRuntimeException("EventHub client should not be null");
        }

        try {
            if (partitionSupplier == null) {
                return client.send(eventData);
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionId())) {
                return client.createPartitionSender(partitionSupplier.getPartitionId())
                        .thenAcceptAsync(s -> s.send(eventData));
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionKey())) {
                return client.send(eventData, partitionSupplier.getPartitionKey());
            } else {
                return client.send(eventData);
            }
        } catch (EventHubException e) {
            throw new EventHubRuntimeException("Failed to send event hub data to " + eventHubName, e);
        }
    }
}
