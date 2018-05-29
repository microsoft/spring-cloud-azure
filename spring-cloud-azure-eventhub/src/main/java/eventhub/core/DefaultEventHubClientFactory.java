/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.management.eventhub.AuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationKey;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.microsoft.azure.spring.cloud.context.core.AzureUtil;
import eventhub.integration.AzureAdmin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 */
@Component
public class DefaultEventHubClientFactory implements EventHubClientFactory, DisposableBean {
    private static final Log LOGGER = LogFactory.getLog(DefaultEventHubClientFactory.class);
    // eventHubName -> eventHubClient
    private final ConcurrentHashMap<String, EventHubClient> clientMap = new ConcurrentHashMap<>();

    // eventHubName -> connectionString
    private final ConcurrentHashMap<String, String> connectionStringMap = new ConcurrentHashMap<>();

    // (eventHubName, partitionId) -> partitionSender
    private final ConcurrentHashMap<Tuple<String, String>, PartitionSender> partitionSenderMap =
            new ConcurrentHashMap<>();

    // (eventHubName, consumerGroup) -> eventProcessorHost
    private final ConcurrentHashMap<Tuple<String, String>, EventProcessorHost> processorHostMap =
            new ConcurrentHashMap<>();

    @Autowired
    private AzureAdmin azureAdmin;

    @Autowired
    private AzureEventHubProperties eventHubProperties;

    @Override
    public EventHubClient getOrCreateEventHubClient(String eventHubName) {
        return this.clientMap.computeIfAbsent(eventHubName, key -> {

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
        return this.partitionSenderMap.computeIfAbsent(new Tuple(eventHubName, partitionId), key -> {

            try {
                return getOrCreateEventHubClient(eventHubName).createPartitionSenderSync(partitionId);
            } catch (EventHubException e) {
                throw new EventHubRuntimeException("Error when creating event hub partition sender", e);
            }
        });
    }

    @Override
    public EventProcessorHost getOrCreateEventProcessorHost(String eventHubName, String consumerGroup) {
        return this.processorHostMap.computeIfAbsent(new Tuple(eventHubName, consumerGroup),
                key -> new EventProcessorHost(EventProcessorHost.createHostName("hostNamePrefix"), eventHubName,
                        consumerGroup, getOrCreateConnectionString(eventHubName), AzureUtil.getConnectionString(
                        azureAdmin.getOrCreateStorageAccount(eventHubProperties.getCheckpointStorageAccount())),
                        eventHubProperties.getCheckpointStorageAccountContainer()));
    }

    private String getOrCreateConnectionString(String eventHubName) {

        return this.connectionStringMap.computeIfAbsent(eventHubName,
                name -> azureAdmin.getEventHub(name).listAuthorizationRules().stream().findFirst()
                                  .map(AuthorizationRule::getKeys)
                                  .map(EventHubAuthorizationKey::primaryConnectionString).orElseThrow(
                                () -> new EventHubRuntimeException(
                                        String.format("Failed to fetch connection string of '%s'", eventHubName),
                                        null)));
    }

    @Override
    public void destroy() throws Exception {
        Stream<CompletableFuture<Void>> closeClientFutures = clientMap.values().stream().map(EventHubClient::close);
        Stream<CompletableFuture<Void>> closeSenderFutures =
                partitionSenderMap.values().stream().map(PartitionSender::close);
        Stream<CompletableFuture<Void>> closeProcessorFutures =
                processorHostMap.values().stream().map(EventProcessorHost::unregisterEventProcessor);
        CompletableFuture.allOf(Stream.of(closeClientFutures, closeSenderFutures, closeProcessorFutures)
                                      .toArray(CompletableFuture[]::new)).exceptionally((ex) -> {
            LOGGER.warn("Failed to clean event hub client factory", ex);
            return null;
        });
    }
}
