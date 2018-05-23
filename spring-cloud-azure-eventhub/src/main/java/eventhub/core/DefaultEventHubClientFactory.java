/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 */
public class DefaultEventHubClientFactory implements EventHubClientFactory {
    private final ConcurrentHashMap<String, EventHubClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PartitionSenderKey, PartitionSender> partitionSenders = new ConcurrentHashMap<>();

    @Override
    public EventHubClient getOrCreateEventHubClient(String eventHubName) {
        return this.clients.computeIfAbsent(eventHubName, key -> {
            //TODO: figure out where to get properties to build connection string
            ConnectionStringBuilder builder = new ConnectionStringBuilder()
                    .setNamespaceName("----ServiceBusNamespaceName-----")
                    .setEventHubName(eventHubName)
                    .setSasKeyName("-----SharedAccessSignatureKeyName-----")
                    .setSasKey("---SharedAccessSignatureKey----");

            try {
                return EventHubClient.createSync(builder.toString(), Executors.newSingleThreadExecutor());
            } catch (EventHubException | IOException e) {
                throw new EventHubRuntimeException("Error when creating event hub client", e);
            }
        });
    }

    @Override
    public PartitionSender getOrCreatePartitionSender(String eventHubName, String partitionId) {
        return this.partitionSenders.computeIfAbsent(new PartitionSenderKey(eventHubName, partitionId), key -> {

            try {
                return getOrCreateEventHubClient(eventHubName).createPartitionSenderSync(partitionId);
            } catch (EventHubException e) {
                throw new EventHubRuntimeException("Error when creating event hub partition sender", e);
            }
        });
    }

    //TODO: clean up all clients and partition sender when close this

    static class PartitionSenderKey {
        private final String eventHubName;
        private final String partition;

        PartitionSenderKey(String eventHubName, String partition) {
            this.eventHubName = eventHubName;
            this.partition = partition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PartitionSenderKey that = (PartitionSenderKey) o;
            return Objects.equals(eventHubName, that.eventHubName) &&
                    Objects.equals(partition, that.partition);
        }

        @Override
        public int hashCode() {

            return Objects.hash(eventHubName, partition);
        }
    }
}
