/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.inbound;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.SubscribeOperation;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.eventhub.inbound.ListenerMode;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class ServiceBusQueueInboundChannelAdapter extends MessageProducerSupport {
    private final String destination;
    private final SubscribeOperation<IMessage> subscribeOperation;
    private CheckpointMode checkpointMode = CheckpointMode.RECORD;
    private ListenerMode listenerMode = ListenerMode.RECORD;
    private MessageConverter messageConverter;
    private Map<String, Object> commonHeaders = new HashMap<>();

    public ServiceBusQueueInboundChannelAdapter(String destination,
            @NonNull SubscribeOperation<IMessage> subscribeOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
        this.subscribeOperation = subscribeOperation;
    }

    @Override
    protected void doStart() {
        super.doStart();

        this.subscribeOperation.subscribe(this.destination, this::receiveMessage);

        if (this.checkpointMode == CheckpointMode.MANUAL) {
            // Send the checkpointer downstream so user decides on when to checkpoint.
            this.commonHeaders.put(AzureHeaders.CHECKPOINTER, subscribeOperation.getCheckpointer(this.destination));
        }
    }

    public void receiveMessage(Iterable<IMessage> events) {

        if (this.listenerMode == ListenerMode.BATCH) {
            sendMessage(toMessage(events));
        } else /* ListenerMode.RECORD */ {
            StreamSupport.stream(events.spliterator(), false).forEach((e) -> {
                sendMessage(toMessage(e.getBody()));
                if (this.checkpointMode == CheckpointMode.RECORD) {
                    this.subscribeOperation.getCheckpointer(destination).checkpoint(e);
                }
            });
        }

        if (this.checkpointMode == CheckpointMode.BATCH) {
            this.subscribeOperation.getCheckpointer(destination).checkpoint();
        }

    }

    private Message<?> toMessage(Object payload) {
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }
        return this.messageConverter.toMessage(payload, new MessageHeaders(commonHeaders));
    }

    @Override
    protected void doStop() {
        this.subscribeOperation.unsubscribe(destination, this::receiveMessage);

        super.doStop();
    }

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    public ListenerMode getListenerMode() {
        return listenerMode;
    }

    public void setListenerMode(ListenerMode listenerMode) {
        this.listenerMode = listenerMode;
    }

    public MessageConverter getMessageConverter() {
        return this.messageConverter;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
