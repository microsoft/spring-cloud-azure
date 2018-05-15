/*
 *  Copyright 2017-2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eventhub.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Strings;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        }
        catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted", e);
            throw new EventHubRuntimeException("Thread interrupted", e);
        }
        catch (ExecutionException e) {
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
            }
            else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionId())) {
                return client.createPartitionSender(partitionSupplier.getPartitionId())
                        .thenAcceptAsync(s -> s.send(eventData));
            }
            else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionKey())) {
                return client.send(eventData, partitionSupplier.getPartitionKey());
            }
            else {
                return client.send(eventData);
            }
        }
        catch (EventHubException e) {
            throw new EventHubRuntimeException("Failed to send event hub data to " + eventHubName, e);
        }
    }
}
