/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.google.common.base.Strings;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventprocessorhost.*;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubCheckpointer;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventHubOperation}.
 *
 * <p>
 * The main event hub component for sending to and consuming from event hub
 *
 * @author Warren Zhu
 */
public class EventHubTemplate implements EventHubOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubTemplate.class);
    private final ConcurrentHashMap<Tuple<String, String>, Set<Consumer<Iterable<EventData>>>>
            consumersByNameAndConsumerGroup = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tuple<String, String>, EventHubCheckpointer> checkpointersByNameAndConsumerGroup =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tuple<String, String>, EventProcessorHost> processorHostsByNameAndConsumerGroup =
            new ConcurrentHashMap<>();

    private final EventHubClientFactory clientFactory;

    @Setter
    private MessageConverter messageConverter = new MappingJackson2MessageConverter();

    @Setter
    private StartPosition startPosition = StartPosition.LATEST;

    public EventHubTemplate(EventHubClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    private static EventProcessorOptions buildEventProcessorOptions(StartPosition startPosition) {
        EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();

        if (startPosition == StartPosition.EARLISET) {
            options.setInitialPositionProvider((s) -> EventPosition.fromStartOfStream());
        } else /* StartPosition.LATEST */ {
            options.setInitialPositionProvider((s) -> EventPosition.fromEndOfStream());
        }

        return options;
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String eventHubName, @NonNull Message<T> message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(eventHubName, "eventHubName can't be null or empty");
        EventData eventData = toEventData(message);
        try {
            EventHubClient client = this.clientFactory.getEventHubClientCreator().apply(eventHubName);

            if (partitionSupplier == null) {
                return client.send(eventData);
            } else if (!Strings.isNullOrEmpty(partitionSupplier.getPartitionId())) {
                return this.clientFactory.getPartitionSenderCreator()
                                         .apply(Tuple.of(client, partitionSupplier.getPartitionId())).send(eventData);
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
    public Checkpointer<EventData> getCheckpointer(String destination, String consumerGroup) {
        return checkpointersByNameAndConsumerGroup.get(Tuple.of(destination, consumerGroup));
    }

    @Override
    public synchronized boolean subscribe(String destination, Consumer<Iterable<EventData>> consumer,
            String consumerGroup) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);
        consumersByNameAndConsumerGroup.putIfAbsent(nameAndConsumerGroup, new CopyOnWriteArraySet<>());
        boolean added = consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).add(consumer);

        if (!added) {
            return false;
        }

        processorHostsByNameAndConsumerGroup.computeIfAbsent(nameAndConsumerGroup, key -> {
            EventProcessorHost host = this.clientFactory.getProcessorHostCreator().apply(key);
            host.registerEventProcessorFactory(context -> new EventHubProcessor(key),
                    buildEventProcessorOptions(startPosition));
            return host;
        });
        return true;
    }

    @Override
    public synchronized boolean unsubscribe(String destination, Consumer<Iterable<EventData>> consumer,
            String consumerGroup) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (!consumersByNameAndConsumerGroup.containsKey(nameAndConsumerGroup)) {
            return false;
        }

        boolean existed = consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).remove(consumer);
        if (consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).isEmpty()) {
            processorHostsByNameAndConsumerGroup.remove(nameAndConsumerGroup).unregisterEventProcessor();
        }

        return existed;
    }

    protected EventData toEventData(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof EventData) {
            return (EventData) payload;
        }

        if (payload instanceof String) {
            return EventData.create(((String) payload).getBytes(Charset.defaultCharset()));
        }

        if (payload instanceof byte[]) {
            return EventData.create((byte[]) payload);
        }

        return EventData.create((byte[]) this.messageConverter.fromMessage(message, byte[].class));
    }

    private class EventHubProcessor implements IEventProcessor {

        private final Tuple<String, String> nameAndConsumerGroup;

        EventHubProcessor(Tuple<String, String> nameAndConsumerGroup) {
            this.nameAndConsumerGroup = nameAndConsumerGroup;
        }

        @Override
        public void onOpen(PartitionContext context) throws Exception {
            LOGGER.info("Partition {} is opening", context.getPartitionId());
            checkpointersByNameAndConsumerGroup.putIfAbsent(nameAndConsumerGroup, new EventHubCheckpointer());
            checkpointersByNameAndConsumerGroup.get(nameAndConsumerGroup).addPartitionContext(context);
        }

        @Override
        public void onClose(PartitionContext context, CloseReason reason) throws Exception {
            LOGGER.info("Partition {} is closing for reason {}", context.getPartitionId(), reason);
            checkpointersByNameAndConsumerGroup.get(nameAndConsumerGroup).removePartitionContext(context);
        }

        @Override
        public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
            consumersByNameAndConsumerGroup.get(nameAndConsumerGroup).forEach(c -> c.accept(events));
        }

        @Override
        public void onError(PartitionContext context, Throwable error) {
            LOGGER.error("Partition {} onError", context.getPartitionId(), error);
        }
    }
}
