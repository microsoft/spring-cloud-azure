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
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.azure.spring.integration.core.Memoizer;
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
    private final Function<Tuple<String, String>, ISubscriptionClient> subscriptionClientCreator =
            Memoizer.memoize(this::createSubscriptionClient);

    private final Function<String, ? extends IMessageSender> sendCreator = Memoizer.memoize(this::createTopicClient);

    public DefaultServiceBusTopicClientFactory(AzureAdmin azureAdmin, String namespace) {
        super(azureAdmin, namespace);
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
        Topic topic = azureAdmin.getServiceBusTopic(namespace, nameAndSubscription.getFirst());
        Assert.notNull(topic,
                () -> String.format("Service bus topic '%s' not existed", nameAndSubscription.getFirst()));

        azureAdmin.getOrCreateServiceBusTopicSubscription(topic, nameAndSubscription.getSecond());

        try {
            return new SubscriptionClient(
                    new ConnectionStringBuilder(getConnectionStringCreator().apply(nameAndSubscription.getFirst())),
                    ReceiveMode.PEEKLOCK);
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus subscription client", e);
        }
    }

    private IMessageSender createTopicClient(String destination) {
        azureAdmin.getOrCreateServiceBusTopic(namespace, destination);
        try {
            return new TopicClient(new ConnectionStringBuilder(getConnectionStringCreator().apply(destination)));
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus topic client", e);
        }
    }
}
