/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.google.common.base.Strings;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.Tuple;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubCheckpointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventHubOperation}.
 *
 * <p>
 * The main eventhub component for sending to and consuming from event hub
 *
 * @author Warren Zhu
 */
public class EventHubTemplate implements EventHubOperation {

    private static final Log LOGGER = LogFactory.getLog(EventHubTemplate.class);
    private final ConcurrentHashMap<Tuple<String, String>, Set<Consumer<Iterable<EventData>>>>
            consumersByNameAndConsumerGroup = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tuple<String, String>, EventHubCheckpointer> checkpointersByNameAndConsumerGroup =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tuple<String, String>, EventProcessorHost> processorHostsByNameAndConsumerGroup =
            new ConcurrentHashMap<>();

    private final EventHubClientFactory clientFactory;

    public EventHubTemplate(EventHubClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public CompletableFuture<Void> sendAsync(String eventHubName, EventData message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(eventHubName, "eventHubName can't be null or empty");
        Assert.notNull(message, "message can't be null");
        try {
            EventHubClient client = this.clientFactory.getOrCreateEventHubClient(eventHubName);

            if (partitionSupplier == null) {
                return client.send(message);
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionId())) {
                return this.clientFactory.getOrCreatePartitionSender(eventHubName, partitionSupplier.getPartitionId())
                                         .send(message);
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionKey())) {
                return client.send(message, partitionSupplier.getPartitionKey());
            } else {
                return client.send(message);
            }
        } catch (EventHubRuntimeException e) {
            LOGGER.error(String.format("Failed to send to '%s' ", eventHubName), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public Checkpointer<EventData> getCheckpointer(String destination, String consumerGroup) {
        return checkpointersByNameAndConsumerGroup.get(new Tuple<>(destination, consumerGroup));
    }

    @Override
    public synchronized boolean subscribe(String destination, Consumer<Iterable<EventData>> consumer,
            String consumerGroup) {
        Tuple<String, String> nameAndConsumerGroup = new Tuple<>(destination, consumerGroup);
        consumersByNameAndConsumerGroup.putIfAbsent(nameAndConsumerGroup, new CopyOnWriteArraySet<>());
        boolean added = consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).add(consumer);

        if (!added) {
            return false;
        }

        processorHostsByNameAndConsumerGroup.computeIfAbsent(nameAndConsumerGroup, key -> {
            EventProcessorHost host = this.clientFactory.getOrCreateEventProcessorHost(destination, consumerGroup);
            host.registerEventProcessorFactory(context -> new IEventProcessor() {

                @Override
                public void onOpen(PartitionContext context) throws Exception {
                    LOGGER.info(String.format("Partition %s is opening", context.getPartitionId()));
                    checkpointersByNameAndConsumerGroup.putIfAbsent(nameAndConsumerGroup, new EventHubCheckpointer());
                    checkpointersByNameAndConsumerGroup.get(new Tuple<>(destination, consumerGroup))
                                                       .addPartitionContext(context);
                }

                @Override
                public void onClose(PartitionContext context, CloseReason reason) throws Exception {
                    LOGGER.info(
                            String.format("Partition %s is closing for reason %s", context.getPartitionId(), reason));
                    checkpointersByNameAndConsumerGroup.get(new Tuple<>(destination, consumerGroup))
                                                       .removePartitionContext(context);
                }

                @Override
                public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
                    consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).forEach(c -> c.accept(events));
                }

                @Override
                public void onError(PartitionContext context, Throwable error) {
                    LOGGER.error(String.format("Partition %s onError", context.getPartitionId()), error);
                }
            });
            return host;
        });

        return true;
    }

    @Override
    public synchronized boolean unsubscribe(String destination, Consumer<Iterable<EventData>> consumer,
            String consumerGroup) {
        Tuple<String, String> nameAndConsumerGroup = new Tuple<>(destination, consumerGroup);
        boolean existed = consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).remove(consumer);
        if (consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).isEmpty()) {
            processorHostsByNameAndConsumerGroup.remove(nameAndConsumerGroup).unregisterEventProcessor();
        }

        return existed;
    }
}
