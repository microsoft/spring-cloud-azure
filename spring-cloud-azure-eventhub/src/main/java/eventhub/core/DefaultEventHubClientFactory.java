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
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 */
@Component
public class DefaultEventHubClientFactory implements EventHubClientFactory, DisposableBean {
    // eventHubName -> eventHubClient
    private final ConcurrentHashMap<String, EventHubClient> clients = new ConcurrentHashMap<>();

    // eventHubName -> connectionString
    private final ConcurrentHashMap<String, String> connectionStrings = new ConcurrentHashMap<>();

    // (eventHubName, partitionId) -> partitionSender
    private final ConcurrentHashMap<Tuple<String, String>, PartitionSender> partitionSenders =
            new ConcurrentHashMap<>();

    // (eventHubName, consumerGroup) -> eventProcessorHost
    private final ConcurrentHashMap<Tuple<String, String>, EventProcessorHost> processorHosts =
            new ConcurrentHashMap<>();

    @Autowired
    private Azure.Authenticated authenticated;

    @Autowired
    private AzureProperties azureProperties;

    @Autowired
    private AzureEventHubProperties eventHubProperties;

    @Override
    public EventHubClient getOrCreateEventHubClient(String eventHubName) {
        return this.clients.computeIfAbsent(eventHubName, key -> {

            try {
                return EventHubClient
                        .createSync(getOrCreateConnectionString(eventHubName), Executors.newSingleThreadExecutor());
            } catch (EventHubException | IOException e) {
                throw new EventHubRuntimeException("Error when creating event hub client", e);
            }
        });
    }

    @Override
    public PartitionSender getOrCreatePartitionSender(String eventHubName, String partitionId) {
        return this.partitionSenders.computeIfAbsent(new Tuple(eventHubName, partitionId), key -> {

            try {
                return getOrCreateEventHubClient(eventHubName).createPartitionSenderSync(partitionId);
            } catch (EventHubException e) {
                throw new EventHubRuntimeException("Error when creating event hub partition sender", e);
            }
        });
    }

    @Override
    public EventProcessorHost getOrCreateEventProcessorHost(String eventHubName, String consumerGroup) {
        return this.processorHosts.computeIfAbsent(new Tuple(eventHubName, consumerGroup),
                key -> new EventProcessorHost(EventProcessorHost.createHostName("hostNamePrefix"), eventHubName,
                        consumerGroup, getOrCreateConnectionString(eventHubName), "storageConnectionString",
                        "storageContainerName"));
    }

    private String getOrCreateConnectionString(String eventHubName) {
        return this.connectionStrings.computeIfAbsent(eventHubName, key -> {
            //TODO: get all properties from management api, pending on no way to get access way
            return new ConnectionStringBuilder().setNamespaceName(eventHubProperties.getNamespace())
                                                .setEventHubName(eventHubName)
                                                .setSasKeyName("-----SharedAccessSignatureKeyName-----")
                                                .setSasKey("---SharedAccessSignatureKey----").toString();
        });
    }

    @Override
    public void destroy() throws Exception {
        clients.values().stream().forEach(EventHubClient::close);
        partitionSenders.values().stream().forEach(PartitionSender::close);
        processorHosts.values().stream().forEach(EventProcessorHost::unregisterEventProcessor);
    }
}
