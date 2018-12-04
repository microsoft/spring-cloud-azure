/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.impl;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementation of {@link IEventProcessor} to be registered via event hub sdk.
 * <p>
 * Mainly handle message conversion and checkpoint
 *
 * @author Warren Zhu
 */
@Slf4j
public class EventHubProcessor implements IEventProcessor {
    private final Consumer<Message<?>> consumer;
    private final Class<?> payloadType;
    private final CheckpointConfig checkpointConfig;
    private final EventHubMessageConverter messageConverter;
    private final EventHubCheckpointManager checkpointManager;

    public EventHubProcessor(Consumer<Message<?>> consumer, Class<?> payloadType, CheckpointConfig checkpointConfig,
            EventHubMessageConverter messageConverter) {
        this.consumer = consumer;
        this.payloadType = payloadType;
        this.checkpointConfig = checkpointConfig;
        this.messageConverter = messageConverter;
        this.checkpointManager = new EventHubCheckpointManager(checkpointConfig);
    }

    @Override
    public void onOpen(PartitionContext context) throws Exception {
        log.info("Partition {} is opening", context.getPartitionId());
    }

    @Override
    public void onClose(PartitionContext context, CloseReason reason) throws Exception {
        log.info("Partition {} is closing for reason {}", context.getPartitionId(), reason);
    }

    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.PARTITION_ID, context.getPartitionId());

        for (EventData e : events) {
            Checkpointer checkpointer = new AzureCheckpointer(() -> context.checkpoint(e));
            if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }
            this.consumer.accept(messageConverter.toMessage(e, new MessageHeaders(headers), payloadType));

            this.checkpointManager.onMessage(context, e);
        }

        this.checkpointManager.completeBatch(context);
    }

    @Override
    public void onError(PartitionContext context, Throwable error) {
        log.error("Partition {} onError", context.getPartitionId(), error);
    }
}
