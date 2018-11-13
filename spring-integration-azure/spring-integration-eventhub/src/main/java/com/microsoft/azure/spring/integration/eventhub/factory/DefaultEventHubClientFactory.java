/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.factory;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.impl.EventHubClientImpl;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubRuntimeException;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.util.HostnameHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 */
@Slf4j
public class DefaultEventHubClientFactory implements EventHubClientFactory, DisposableBean {
    private static final String PROJECT_VERSION =
            DefaultEventHubClientFactory.class.getPackage().getImplementationVersion();
    private static final String USER_AGENT = "spring-cloud-azure/" + PROJECT_VERSION;

    // Maps used for cache and clean up clients
    private final Map<String, EventHubClient> clientsByName = new ConcurrentHashMap<>();
    // (eventHubClient, partitionId) -> partitionSender
    private final Map<Tuple<EventHubClient, String>, PartitionSender> partitionSenderMap = new ConcurrentHashMap<>();
    // (eventHubName, consumerGroup) -> eventProcessorHost
    private final Map<Tuple<String, String>, EventProcessorHost> processorHostMap = new ConcurrentHashMap<>();
    private final BiFunction<EventHubClient, String, PartitionSender> partitionSenderCreator =
            Memoizer.memoize(partitionSenderMap, this::createPartitionSender);
    private final Function<String, String> connectionStringProvider;
    // Memoized functional client creator
    private final Function<String, EventHubClient> eventHubClientCreator =
            Memoizer.memoize(clientsByName, this::createEventHubClient);
    private final String checkpointStorageConnectionString;
    private final BiFunction<String, String, EventProcessorHost> processorHostCreator =
            Memoizer.memoize(processorHostMap, this::createEventProcessorHost);

    public DefaultEventHubClientFactory(String checkpointConnectionString,
            Function<String, String> connectionStringProvider) {
        Assert.hasText(checkpointConnectionString, "checkpointConnectionString can't be null or empty");
        this.connectionStringProvider = connectionStringProvider;
        this.checkpointStorageConnectionString = checkpointConnectionString;
        EventHubClientImpl.USER_AGENT = USER_AGENT + "/" + EventHubClientImpl.USER_AGENT;
    }

    private EventHubClient createEventHubClient(String eventHubName) {
        try {
            return EventHubClient
                    .createSync(connectionStringProvider.apply(eventHubName), Executors.newSingleThreadExecutor());
        } catch (EventHubException | IOException e) {
            throw new EventHubRuntimeException("Error when creating event hub client", e);
        }
    }

    private PartitionSender createPartitionSender(EventHubClient client, String partitionId) {
        try {
            return client.createPartitionSenderSync(partitionId);
        } catch (EventHubException e) {
            throw new EventHubRuntimeException("Error when creating event hub partition sender", e);
        }
    }

    private EventProcessorHost createEventProcessorHost(String name, String consumerGroup) {
        return new EventProcessorHost(EventProcessorHost.createHostName(HostnameHelper.getHostname()), name,
                consumerGroup, connectionStringProvider.apply(name), checkpointStorageConnectionString, name);
    }

    private <K, V> void close(Map<K, V> map, Function<V, CompletableFuture<Void>> close) {
        CompletableFuture.allOf(map.values().stream().map(close).toArray(CompletableFuture[]::new))
                         .exceptionally((ex) -> {
                             log.warn("Failed to clean event hub client factory", ex);
                             return null;
                         });
    }

    @Override
    public void destroy() throws Exception {
        close(clientsByName, EventHubClient::close);
        close(partitionSenderMap, PartitionSender::close);
        close(processorHostMap, EventProcessorHost::unregisterEventProcessor);
    }

    @Override
    public EventHubClient getOrCreateClient(String name) {
        return this.eventHubClientCreator.apply(name);
    }

    @Override
    public PartitionSender getOrCreatePartitionSender(String eventhub, String partition) {
        return this.partitionSenderCreator.apply(getOrCreateClient(eventhub), partition);
    }

    @Override
    public EventProcessorHost getOrCreateEventProcessorHost(String name, String consumerGroup) {
        return this.processorHostCreator.apply(name, consumerGroup);
    }
}
