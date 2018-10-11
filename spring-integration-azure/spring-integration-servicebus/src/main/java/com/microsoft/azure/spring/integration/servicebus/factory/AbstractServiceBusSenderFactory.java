/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;

import java.util.function.Function;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
public abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {
    protected final ResourceManagerProvider resourceManagerProvider;
    protected final String namespace;
    protected final ServiceBusNamespace serviceBusNamespace;
    protected final Function<String, String> connectionStringCreator;

    AbstractServiceBusSenderFactory(ResourceManagerProvider resourceManagerProvider, String namespace) {
        this.resourceManagerProvider = resourceManagerProvider;
        this.namespace = namespace;
        this.serviceBusNamespace = this.resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(namespace);
        ServiceBusConnectionStringProvider provider =
                new ServiceBusConnectionStringProvider(this.resourceManagerProvider.getServiceBusNamespaceManager());
        this.connectionStringCreator = provider::getConnectionString;
    }
}
