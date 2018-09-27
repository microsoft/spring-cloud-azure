/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.spring.integration.core.AzureCheckpointer;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementation of {@link IEventProcessor} to be registered vis event hub sdk.
 * <p>
 * Mainly handle message conversion and checkpoint
 *
 * @author Warren Zhu
 */
@AllArgsConstructor
public class EventHubProcessor implements IEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubProcessor.class);
    private final Consumer<Message<?>> consumer;
    private final Class<?> payloadType;
    private final CheckpointMode checkpointMode;
    private final EventHubMessageConverter messageConverter;

    @Override
    public void onOpen(PartitionContext context) throws Exception {
        LOGGER.info("Partition {} is opening", context.getPartitionId());
    }

    @Override
    public void onClose(PartitionContext context, CloseReason reason) throws Exception {
        LOGGER.info("Partition {} is closing for reason {}", context.getPartitionId(), reason);
    }

    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.PARTITION_ID, context.getPartitionId());

        for (EventData e : events) {
            Checkpointer checkpointer = new AzureCheckpointer(() -> context.checkpoint(e));
            if (checkpointMode == CheckpointMode.MANUAL) {
                headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
            }
            this.consumer.accept(messageConverter.toMessage(e, new MessageHeaders(headers), payloadType));

            if (checkpointMode == CheckpointMode.RECORD) {
                checkpointer.success().whenComplete((s, t) -> {
                    if (t != null) {
                        LOGGER.warn("Failed to checkpoint", t);
                    }
                });
            }
        }

        if (checkpointMode == CheckpointMode.BATCH) {
            context.checkpoint().whenComplete((s, t) -> {
                if (t != null) {
                    LOGGER.warn("Failed to checkpoint", t);
                }
            });
        }
    }

    @Override
    public void onError(PartitionContext context, Throwable error) {
        LOGGER.error("Partition {} onError", context.getPartitionId(), error);
    }
}
