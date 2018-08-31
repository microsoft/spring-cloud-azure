/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.Memoizer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;

import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusQueueClientFactory}.
 * Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusQueueClientFactory extends AbstractServiceBusSenderFactory
        implements ServiceBusQueueClientFactory {

    private final Function<String, IQueueClient> queueClientCreator = Memoizer.memoize(this::createQueueClient);

    public DefaultServiceBusQueueClientFactory(AzureAdmin azureAdmin, String namespace) {
        super(azureAdmin, namespace);
    }

    @Override
    public Function<String, IQueueClient> getQueueClientCreator() {
        return queueClientCreator;
    }

    @Override
    public Function<String, IQueueClient> getSenderCreator() {
        return getQueueClientCreator();
    }

    private IQueueClient createQueueClient(String destination) {
        azureAdmin.getOrCreateServiceBusQueue(namespace, destination);
        try {
            return new QueueClient(new ConnectionStringBuilder(getConnectionStringCreator().apply(destination)),
                    ReceiveMode.PEEKLOCK);
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus queue client", e);
        }
    }
}
