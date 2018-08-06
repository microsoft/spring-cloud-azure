/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */


package com.microsoft.azure.spring.integration.storagequeue.inbound;


import com.microsoft.azure.spring.integration.core.QueueOperation;
import com.microsoft.azure.spring.integration.core.SubscribeOperation;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StorageQueueInboundChannelAdapter extends MessageProducerSupport {
    private final String destination;
    private final QueueOperation<CloudQueueMessage> queueOperation;
    private MessageConverter messageConverter;
    private Map<String, Object> commonHeaders = new HashMap<>();

    public StorageQueueInboundChannelAdapter(String destination,
                                             QueueOperation<CloudQueueMessage> queueOperation) {
        this.destination = destination;
        this.queueOperation = queueOperation;
    }

    @Override
    protected void doStart(){
        super.doStart();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(
                this::receiveMessage, 10, 10, TimeUnit.SECONDS);
    }

    private void receiveMessage() {
        CloudQueueMessage cloudQueueMessage = queueOperation.retrieve();
        sendMessage(toMessage(cloudQueueMessage));
    }

    private Message<?> toMessage(Object payload) {
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }
        return this.messageConverter.toMessage(payload, new MessageHeaders(commonHeaders));
    }


}
