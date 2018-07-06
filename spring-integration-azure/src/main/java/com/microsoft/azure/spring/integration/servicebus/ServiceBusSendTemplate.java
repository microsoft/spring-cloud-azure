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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Azure service bus send template to support send {@link IMessage} asynchronously
 *
 * @author Warren Zhu
 */
public class ServiceBusSendTemplate<T extends ServiceBusSenderFactory> implements SendOperation<IMessage> {
    protected final T senderFactory;

    public ServiceBusSendTemplate(@NonNull T senderFactory) {
        this.senderFactory = senderFactory;
    }

    @Override
    public CompletableFuture<Void> sendAsync(String destination, @NonNull IMessage message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        String partitionKey = getPartitionKey(partitionSupplier);

        if (StringUtils.hasText(partitionKey)) {
            message.setPartitionKey(partitionKey);
        }

        return this.senderFactory.getSenderCreator().apply(destination).sendAsync(message);
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
