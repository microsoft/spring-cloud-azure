/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.core.Memoizer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusSendTemplate;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
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
 * Default implementation of {@link ServiceBusTopicOperation}.
 *
 * @author Warren Zhu
 */
public class ServiceBusTopicTemplate extends ServiceBusSendTemplate<ServiceBusTopicClientFactory>
        implements ServiceBusTopicOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTopicTemplate.class);

    private final Map<Tuple<String, String>, Consumer<IMessage>> consumerByNameAndConsumerGroup =
            new ConcurrentHashMap<>();
    private final Function<Tuple<String, String>, Checkpointer<UUID>> checkpointGetter =
            Memoizer.memoize(this::createCheckpointer);

    public ServiceBusTopicTemplate(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public boolean subscribe(String destination, @NonNull Consumer<IMessage> consumer,
            @NonNull String consumerGroup) {
        Assert.hasText(destination, "destination can't be null or empty");

        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (consumerByNameAndConsumerGroup.containsKey(nameAndConsumerGroup)) {
            return false;
        }

        consumerByNameAndConsumerGroup.put(nameAndConsumerGroup, consumer);

        try {
            this.senderFactory.getSubscriptionClientCreator().apply(Tuple.of(destination, consumerGroup))
                              .registerMessageHandler(new ServiceBusMessageHandler(
                                      consumerByNameAndConsumerGroup.get(nameAndConsumerGroup)));
        } catch (ServiceBusException | InterruptedException e) {
            LOGGER.error("Failed to register message handler", e);
            throw new ServiceBusRuntimeException("Failed to register message handler", e);
        }

        return true;
    }

    @Override
    public boolean unsubscribe(String destination, String consumerGroup) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        if (!consumerByNameAndConsumerGroup.containsKey(nameAndConsumerGroup)) {
            return false;
        }

        //TODO: unregister message handler but service bus sdk unsupported

        consumerByNameAndConsumerGroup.remove(nameAndConsumerGroup);

        return true;
    }

    @Override
    public Checkpointer<UUID> getCheckpointer(String destination, String consumerGroup) {
        return this.checkpointGetter.apply(Tuple.of(destination, consumerGroup));
    }

    private Checkpointer<UUID> createCheckpointer(Tuple<String, String> nameAndSubscription) {
        return new ServiceBusTopicCheckpointer(this.senderFactory.getSubscriptionClientCreator().apply(Tuple
                .of(nameAndSubscription.getFirst(), nameAndSubscription.getSecond())));
    }
}
