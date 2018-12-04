/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class EventHubTestOperation extends EventHubTemplate {
    private final Multimap<String, EventData> eventHubsByName = ArrayListMultimap.create();
    private final Map<String, Map<String, EventHubProcessor>> processorsByNameAndGroup = new ConcurrentHashMap<>();
    private final Supplier<PartitionContext> partitionContextSupplier;

    public EventHubTestOperation(EventHubClientFactory clientFactory,
            Supplier<PartitionContext> partitionContextSupplier) {
        super(clientFactory);
        this.partitionContextSupplier = partitionContextSupplier;
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        EventData azureMessage = getMessageConverter().fromMessage(message, EventData.class);

        eventHubsByName.put(name, azureMessage);
        processorsByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());
        processorsByNameAndGroup.get(name).values().forEach(c -> {
            try {
                c.onEvents(partitionContextSupplier.get(), Collections.singleton(azureMessage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        future.complete(null);
        return future;
    }

    @Override
    protected synchronized void register(String name, String group, EventHubProcessor eventProcessor) {
        processorsByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());

        processorsByNameAndGroup.get(name).putIfAbsent(group, eventProcessor);

        if (getStartPosition() == StartPosition.EARLIEST) {
            processorsByNameAndGroup.get(name).values().forEach(c -> {
                try {
                    c.onEvents(partitionContextSupplier.get(), eventHubsByName.get(name));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public boolean unsubscribe(String name, String consumerGroup) {
        processorsByNameAndGroup.get(name).remove(consumerGroup);
        return true;
    }
}

