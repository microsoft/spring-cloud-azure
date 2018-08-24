/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Azure service bus send template to support send {@link IMessage} asynchronously
 *
 * @author Warren Zhu
 */
public class ServiceBusSendTemplate<T extends ServiceBusSenderFactory> implements SendOperation {
    protected final T senderFactory;

    public ServiceBusSendTemplate(@NonNull T senderFactory) {
        this.senderFactory = senderFactory;
    }

    @Override
    public <T> CompletableFuture<Void> sendAsync(String destination, @NonNull Message<T> message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        String partitionKey = getPartitionKey(partitionSupplier);
        IMessage serviceBusMessage = toServiceBusMessage(message);

        if (StringUtils.hasText(partitionKey)) {
            serviceBusMessage.setPartitionKey(partitionKey);
        }

        return this.senderFactory.getSenderCreator().apply(destination).sendAsync(serviceBusMessage);
    }

    protected IMessage toServiceBusMessage(Message<?> message) {
        com.microsoft.azure.servicebus.Message serviceBusMessage;

        Object payload = message.getPayload();
        if (payload instanceof com.microsoft.azure.servicebus.Message) {
            serviceBusMessage = (com.microsoft.azure.servicebus.Message) payload;
        } else if (payload instanceof String) {
            serviceBusMessage =
                    new com.microsoft.azure.servicebus.Message(((String) payload).getBytes(Charset.defaultCharset()));
        } else if (payload instanceof byte[]) {
            serviceBusMessage = new com.microsoft.azure.servicebus.Message((byte[]) payload);
        } else {
            serviceBusMessage = new com.microsoft.azure.servicebus.Message(String.valueOf(payload));
        }

        if (message.getHeaders().containsKey(MessageHeaders.CONTENT_TYPE)) {
            serviceBusMessage.setContentType(message.getHeaders().get(MessageHeaders.CONTENT_TYPE, String.class));
        }

        if (message.getHeaders().containsKey(MessageHeaders.ID)) {
            serviceBusMessage.setMessageId(message.getHeaders().get(MessageHeaders.ID, UUID.class).toString());
        }

        return serviceBusMessage;
    }

    private String getPartitionKey(PartitionSupplier partitionSupplier) {
        if (partitionSupplier == null) {
            return "";
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionKey())) {
            return partitionSupplier.getPartitionKey();
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionId())) {
            return partitionSupplier.getPartitionId();
        }

        return "";
    }
}
