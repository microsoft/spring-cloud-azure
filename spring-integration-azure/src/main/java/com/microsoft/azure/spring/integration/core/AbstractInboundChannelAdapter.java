/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.eventhub.inbound.ListenerMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@Getter
@Setter
public abstract class AbstractInboundChannelAdapter<D> extends MessageProducerSupport {
    private static final String DEFAULT_CONSUMER_GROUP = "$Default";
    private final String destination;
    private final SubscribeByGroupOperation<D> subscribeByGroupOperation;
    protected MessageConverter messageConverter;
    protected Map<String, Object> commonHeaders = new HashMap<>();
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;
    private ListenerMode listenerMode = ListenerMode.RECORD;

    private String consumerGroup = DEFAULT_CONSUMER_GROUP;

    public AbstractInboundChannelAdapter(String destination, SubscribeByGroupOperation<D> subscribeByGroupOperation,
            String consumerGroup) {
        Assert.hasText(destination, "destination can't be null or empty");
        Assert.notNull(subscribeByGroupOperation, "subscribeByGroupOperation can't be null");
        this.destination = destination;
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        if (StringUtils.hasText(consumerGroup)) {
            this.consumerGroup = consumerGroup;
        }
    }

    @Override
    public void doStart() {
        super.doStart();

        this.subscribeByGroupOperation.subscribe(this.destination, this::receiveMessage, this.consumerGroup);

        if (this.checkpointMode == CheckpointMode.MANUAL) {
            // Send the checkpointer downstream so user decides on when to checkpoint.
            this.commonHeaders.put(AzureHeaders.CHECKPOINTER,
                    subscribeByGroupOperation.getCheckpointer(this.destination, this.consumerGroup));
        }
    }

    public void receiveMessage(Iterable<D> events) {

        if (this.listenerMode == ListenerMode.BATCH) {
            sendMessage(toMessage(events));
        } else /* ListenerMode.RECORD */ {
            StreamSupport.stream(events.spliterator(), false).forEach((e) -> {
                sendMessage(toMessage(e));
                if (this.checkpointMode == CheckpointMode.RECORD) {
                    this.subscribeByGroupOperation.getCheckpointer(destination, consumerGroup).checkpoint(e);
                }
            });
        }

        if (this.checkpointMode == CheckpointMode.BATCH) {
            this.subscribeByGroupOperation.getCheckpointer(destination, consumerGroup).checkpoint();
        }

    }

    protected abstract Message<?> toMessage(D data);

    protected abstract Message<?> toMessage(Iterable<D> data);

    @Override
    protected void doStop() {
        this.subscribeByGroupOperation.unsubscribe(destination, this::receiveMessage, this.consumerGroup);

        super.doStop();
    }
}
