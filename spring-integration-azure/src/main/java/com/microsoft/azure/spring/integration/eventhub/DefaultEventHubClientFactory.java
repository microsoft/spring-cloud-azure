/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.impl.EventHubClientImpl;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureUtil;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 */
public class DefaultEventHubClientFactory implements EventHubClientFactory, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubClientFactory.class);
    private static final String PROJECT_VERSION =
            DefaultEventHubClientFactory.class.getPackage().getImplementationVersion();
    private static final String USER_AGENT = "spring-cloud-azure/" + PROJECT_VERSION;

    // Maps used for cache and clean up clients
    private final Map<String, EventHubClient> clientsByName = new ConcurrentHashMap<>();
    // (eventHubClient, partitionId) -> partitionSender
    private final Map<Tuple<EventHubClient, String>, PartitionSender> partitionSenderMap = new ConcurrentHashMap<>();
    // (eventHubName, consumerGroup) -> eventProcessorHost
    private final Map<Tuple<String, String>, EventProcessorHost> processorHostMap = new ConcurrentHashMap<>();

    // Memoized functional client creator
    @Getter
    private final Function<String, EventHubClient> eventHubClientCreator =
            Memoizer.memoize(clientsByName, this::createEventHubClient);
    @Getter
    private final Function<Tuple<String, String>, EventProcessorHost> processorHostCreator =
            Memoizer.memoize(processorHostMap, this::createEventProcessorHost);
    @Getter
    private final Function<Tuple<EventHubClient, String>, PartitionSender> partitionSenderCreator =
            Memoizer.memoize(partitionSenderMap, this::createPartitionSender);
    private final Function<String, String> connectionStringProvider;

    private final AzureAdmin azureAdmin;
    private final String checkpointStorageConnectionString;

    public DefaultEventHubClientFactory(@NonNull AzureAdmin azureAdmin, String checkpointStorageAccount,
            Function<String, String> connectionStringProvider) {
        Assert.hasText(checkpointStorageAccount, "checkpointStorageAccount can't be null or empty");
        this.azureAdmin = azureAdmin;
        this.connectionStringProvider = connectionStringProvider;
        this.checkpointStorageConnectionString = buildConnectionString(checkpointStorageAccount);
        EventHubClientImpl.USER_AGENT = USER_AGENT + "/" + EventHubClientImpl.USER_AGENT;
    }

    private String buildConnectionString(String checkpointStorageAccount) {
        return AzureUtil.getConnectionString(azureAdmin.getOrCreateStorageAccount(checkpointStorageAccount));
    }

    private EventHubClient createEventHubClient(String eventHubName) {
        try {
            return EventHubClient
                    .createSync(connectionStringProvider.apply(eventHubName), Executors.newSingleThreadExecutor());
        } catch (EventHubException | IOException e) {
            throw new EventHubRuntimeException("Error when creating event hub client", e);
        }
    }

    private PartitionSender createPartitionSender(Tuple<EventHubClient, String> clientAndPartitionId) {
        try {
            return clientAndPartitionId.getFirst().createPartitionSenderSync(clientAndPartitionId.getSecond());
        } catch (EventHubException e) {
            throw new EventHubRuntimeException("Error when creating event hub partition sender", e);
        }
    }

    private EventProcessorHost createEventProcessorHost(Tuple<String, String> nameAndConsumerGroup) {
        String eventHubName = nameAndConsumerGroup.getFirst();
        return new EventProcessorHost(EventProcessorHost.createHostName(HostnameHelper.getHostname()), eventHubName,
                nameAndConsumerGroup.getSecond(), connectionStringProvider.apply(eventHubName),
                checkpointStorageConnectionString, eventHubName);
    }

    private <K, V> void close(Map<K, V> map, Function<V, CompletableFuture<Void>> close) {
        CompletableFuture.allOf(map.values().stream().map(close).toArray(CompletableFuture[]::new))
                         .exceptionally((ex) -> {
                             LOGGER.warn("Failed to clean event hub client factory", ex);
                             return null;
                         });
    }

    @Override
    public void destroy() throws Exception {
        close(clientsByName, EventHubClient::close);
        close(partitionSenderMap, PartitionSender::close);
        close(processorHostMap, EventProcessorHost::unregisterEventProcessor);
    }

}
