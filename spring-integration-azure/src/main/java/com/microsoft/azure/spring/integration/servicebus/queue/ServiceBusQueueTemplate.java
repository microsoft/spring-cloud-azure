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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusQueueOperation}.
 *
 * @author Warren Zhu
 */
public class ServiceBusQueueTemplate extends ServiceBusSendTemplate<ServiceBusQueueClientFactory>
        implements ServiceBusQueueOperation {
    private static final Log LOGGER = LogFactory.getLog(ServiceBusQueueTemplate.class);

    private final Map<String, Set<Consumer<Iterable<IMessage>>>> consumersByName = new ConcurrentHashMap<>();
    private final Function<String, Checkpointer<IMessage>> checkpointGetter =
            Memoizer.memoize(this::createCheckpointer);

    public ServiceBusQueueTemplate(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public synchronized boolean subscribe(String destination, @NonNull Consumer<Iterable<IMessage>> consumer) {
        Assert.hasText(destination, "destination can't be null or empty");
        consumersByName.putIfAbsent(destination, new CopyOnWriteArraySet<>());
        boolean added = consumersByName.get(destination).add(consumer);

        try {
            this.senderFactory.getQueueClientCreator().apply(destination)
                              .registerMessageHandler(new ServiceBusMessageHandler(consumersByName.get(destination)));
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register message handler", e);
            throw new ServiceBusRuntimeException("Failed to register message handler", e);
        }

        return added;
    }

    @Override
    public synchronized boolean unsubscribe(String destination, Consumer<Iterable<IMessage>> consumer) {
        boolean existed = consumersByName.get(destination).remove(consumer);

        //TODO: unregister message handler but service bus sdk unsupported

        return existed;
    }

    @Override
    public Checkpointer<IMessage> getCheckpointer(String destination) {
        return checkpointGetter.apply(destination);
    }

    private Checkpointer<IMessage> createCheckpointer(String destination) {
        return new ServiceBusQueueCheckpointer(this.senderFactory.getQueueClientCreator().apply(destination));
    }
}
