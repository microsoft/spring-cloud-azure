/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Azure service bus template to support send {@link Message} asynchronously
 *
 * @author Warren Zhu
 */
@Slf4j
public class ServiceBusTemplate<T extends ServiceBusSenderFactory> implements SendOperation {
    protected final T senderFactory;
    protected final MessageHandlerOptions options = new MessageHandlerOptions(1, false, Duration.ofMinutes(5));

    @Getter
    protected CheckpointConfig checkpointConfig =
            CheckpointConfig.builder().checkpointMode(CheckpointMode.RECORD).build();

    @Getter
    @Setter
    protected ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();

    public ServiceBusTemplate(@NonNull T senderFactory) {
        this.senderFactory = senderFactory;
        log.info("Started ServiceBusTemplate with properties: {}", checkpointConfig);
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String destination, @NonNull Message<U> message,
            PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        String partitionKey = getPartitionKey(partitionSupplier);
        IMessage serviceBusMessage = messageConverter.fromMessage(message, IMessage.class);

        if (StringUtils.hasText(partitionKey)) {
            serviceBusMessage.setPartitionKey(partitionKey);
        }

        return this.senderFactory.getOrCreateSender(destination).sendAsync(serviceBusMessage);
    }

    public void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        Assert.state(isValidCheckpointConfig(checkpointConfig),
                "Only MANUAL or RECORD checkpoint mode is supported in ServiceBusTemplate");
        this.checkpointConfig = checkpointConfig;
        log.info("ServiceBusTemplate checkpoint config becomes: {}", this.checkpointConfig);
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

    private static boolean isValidCheckpointConfig(CheckpointConfig checkpointConfig) {
        return checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL ||
                checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD;
    }
}
