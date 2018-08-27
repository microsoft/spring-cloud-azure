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
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ConcurrentHashMap<Tuple<String, String>, EventProcessorHost> processorHostsByNameAndConsumerGroup =
            new ConcurrentHashMap<>();

    private final EventHubClientFactory clientFactory;

    @Getter
    @Setter
    private EventHubMessageConverter messageConverter = new EventHubMessageConverter();

    @Setter
    private StartPosition startPosition = StartPosition.LATEST;

    @Setter
    private CheckpointMode checkpointMode = CheckpointMode.BATCH;

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
        EventData eventData = messageConverter.fromMessage(message, EventData.class);
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
    @SuppressWarnings("unchecked")
    public boolean subscribe(String destination, String consumerGroup, Consumer<Message<?>> consumer,
            Class<?> messagePayloadType) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (processorHostsByNameAndConsumerGroup.containsKey(nameAndConsumerGroup)) {
            return false;
        }

        processorHostsByNameAndConsumerGroup.computeIfAbsent(nameAndConsumerGroup, key -> {
            EventProcessorHost host = this.clientFactory.getProcessorHostCreator().apply(key);
            host.registerEventProcessorFactory(context -> new EventHubProcessor(consumer, messagePayloadType),
                    buildEventProcessorOptions(startPosition));
            return host;
        });
        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (!processorHostsByNameAndConsumerGroup.containsKey(nameAndConsumerGroup)) {
            return false;
        }

        processorHostsByNameAndConsumerGroup.remove(nameAndConsumerGroup).unregisterEventProcessor();

        return true;
    }

    private class EventHubProcessor<T> implements IEventProcessor {

        private final Consumer<Message<T>> consumer;
        private final Class payloadType;

        EventHubProcessor(@NonNull Consumer<Message<T>> consumer, @NonNull Class<T> payloadType) {
            this.consumer = consumer;
            this.payloadType = payloadType;
        }

        @Override
        public void onOpen(PartitionContext context) throws Exception {
            LOGGER.info("Partition {} is opening", context.getPartitionId());
        }

        @Override
        public void onClose(PartitionContext context, CloseReason reason) throws Exception {
            LOGGER.info("Partition {} is closing for reason {}", context.getPartitionId(), reason);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
            Map<String, Object> headers = new HashMap<>();
            headers.put(AzureHeaders.PARTITION_ID, context.getPartitionId());

            for (EventData e : events) {
                if (checkpointMode == CheckpointMode.MANUAL) {
                    headers.put(AzureHeaders.CHECKPOINTER, new AzureCheckpointer(() -> context.checkpoint(e)));
                }
                this.consumer.accept(messageConverter.toMessage(e, new MessageHeaders(headers), payloadType));
            }

            if (checkpointMode == CheckpointMode.BATCH) {
                context.checkpoint();
            }
        }

        @Override
        public void onError(PartitionContext context, Throwable error) {
            LOGGER.error("Partition {} onError", context.getPartitionId(), error);
        }
    }
}
