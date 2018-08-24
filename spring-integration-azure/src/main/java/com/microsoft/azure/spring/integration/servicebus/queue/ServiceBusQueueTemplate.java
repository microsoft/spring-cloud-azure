/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.Memoizer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusSendTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 */
public class ServiceBusQueueTemplate extends ServiceBusSendTemplate<ServiceBusQueueClientFactory>
        implements ServiceBusQueueOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueTemplate.class);

    private final Map<String, Consumer<IMessage>> consumerByName = new ConcurrentHashMap<>();
    private final Function<String, Checkpointer<UUID>> checkpointGetter = Memoizer.memoize(this::createCheckpointer);

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public boolean subscribe(String destination, @NonNull Consumer<IMessage> consumer) {
        Assert.hasText(destination, "destination can't be null or empty");

        if (consumerByName.containsKey(destination)) {
            return false;
        }

        try {
            this.senderFactory.getQueueClientCreator().apply(destination)
                              .registerMessageHandler(new ServiceBusMessageHandler(consumerByName.get(destination)));
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register message handler", e);
            throw new ServiceBusRuntimeException("Failed to register message handler", e);
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination) {
        consumerByName.remove(destination);
        //TODO: unregister message handler but service bus sdk unsupported

        return true;
    }

    @Override
    public Checkpointer<UUID> getCheckpointer(String destination) {
        return checkpointGetter.apply(destination);
    }

    private Checkpointer<UUID> createCheckpointer(String destination) {
        return new ServiceBusQueueCheckpointer(this.senderFactory.getQueueClientCreator().apply(destination));
    }
}
