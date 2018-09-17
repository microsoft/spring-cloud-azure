/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import org.springframework.util.Assert;

import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}.
 * Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
        implements ServiceBusTopicClientFactory {
    private static final String SUBSCRIPTION_PATH = "%s/subscriptions/%s";
    private final Function<Tuple<String, String>, ISubscriptionClient> subscriptionClientCreator =
            Memoizer.memoize(this::createSubscriptionClient);
    private final Function<String, ? extends IMessageSender> sendCreator = Memoizer.memoize(this::createTopicClient);

    public DefaultServiceBusTopicClientFactory(ResourceManagerProvider resourceManagerProvider, String namespace) {
        super(resourceManagerProvider, namespace);
    }

    @Override
    public Function<Tuple<String, String>, ISubscriptionClient> getSubscriptionClientCreator() {
        return subscriptionClientCreator;
    }

    @Override
    public Function<String, ? extends IMessageSender> getSenderCreator() {
        return sendCreator;
    }

    private ISubscriptionClient createSubscriptionClient(Tuple<String, String> nameAndSubscription) {
        String topicName = nameAndSubscription.getFirst();
        String subscription = nameAndSubscription.getSecond();

        Topic topic = resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of
                (serviceBusNamespace, topicName));

        resourceManagerProvider.getServiceBusTopicSubscriptionManager().getOrCreate(Tuple.of(topic, subscription));

        String subscriptionPath = String.format(SUBSCRIPTION_PATH, topicName, subscription);
        try {
            return new SubscriptionClient(
                    new ConnectionStringBuilder(connectionStringCreator.apply(namespace), subscriptionPath),
                    ReceiveMode.PEEKLOCK);
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus subscription client", e);
        }
    }

    private IMessageSender createTopicClient(String topicName) {
        resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of
                (serviceBusNamespace, topicName));
        try {
            return new TopicClient(new ConnectionStringBuilder(connectionStringCreator.apply(namespace), topicName));
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus topic client", e);
        }
    }
}
