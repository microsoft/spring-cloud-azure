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
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@Getter
public abstract class AbstractInboundChannelAdapter<D, K> extends MessageProducerSupport {
    private final String destination;
    protected String consumerGroup;
    protected SubscribeOperation<D, K> subscribeOperation;
    protected SubscribeByGroupOperation<D, K> subscribeByGroupOperation;
    protected MessageConverter messageConverter;
    protected Map<String, Object> commonHeaders = new HashMap<>();

    @Setter
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;
    @Setter
    private ListenerMode listenerMode = ListenerMode.RECORD;

    protected AbstractInboundChannelAdapter(String destination) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
    }

    @Override
    public void doStart() {
        super.doStart();

        if (useGroupOperation()) {
            this.subscribeByGroupOperation.subscribe(this.destination, this::receiveMessage, this.consumerGroup);
        } else {
            this.subscribeOperation.subscribe(this.destination, this::receiveMessage);
        }

        if (this.checkpointMode == CheckpointMode.MANUAL) {
            // Send the checkpointer downstream so user decides on when to checkpoint.
            this.commonHeaders.put(AzureHeaders.CHECKPOINTER, getCheckpointer());
        }
    }

    public void receiveMessage(Iterable<D> events) {

        if (this.listenerMode == ListenerMode.BATCH) {
            sendMessage(toMessage(events));
        } else /* ListenerMode.RECORD */ {
            StreamSupport.stream(events.spliterator(), false).forEach((e) -> {
                sendMessage(toMessage(e));
                if (this.checkpointMode == CheckpointMode.RECORD) {
                    this.getCheckpointer().checkpoint(getCheckpointKey(e));
                }
            });
        }

        if (this.checkpointMode == CheckpointMode.BATCH) {
            this.getCheckpointer().checkpoint();
        }

    }

    public Message<?> toMessage(D data) {
        Object payload = getPayload(data);
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }

        MessageHeaders headers = new MessageHeaders(commonHeaders);

        if (this.checkpointMode == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINT_KEY, getCheckpointKey(data));
        }

        return this.messageConverter.toMessage(payload, headers);
    }

    protected abstract Object getPayload(D data);

    protected Message<?> toMessage(Iterable<D> data) {
        throw new UnsupportedOperationException();
    }

    protected abstract K getCheckpointKey(D data);

    @Override
    protected void doStop() {
        if (useGroupOperation()) {
            this.subscribeByGroupOperation.unsubscribe(destination, this::receiveMessage, this.consumerGroup);
        } else {
            this.subscribeOperation.unsubscribe(destination, this::receiveMessage);
        }

        super.doStop();
    }

    private Checkpointer<K> getCheckpointer() {
        if (useGroupOperation()) {
            return this.subscribeByGroupOperation.getCheckpointer(this.destination, this.consumerGroup);
        } else {
            return this.subscribeOperation.getCheckpointer(this.destination);
        }
    }

    private boolean useGroupOperation() {
        return this.subscribeByGroupOperation != null && StringUtils.hasText(consumerGroup);
    }

}
