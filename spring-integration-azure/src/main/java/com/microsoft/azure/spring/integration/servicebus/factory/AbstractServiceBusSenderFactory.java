/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureAdmin;

import java.util.function.Function;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
public abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {
    protected final AzureAdmin azureAdmin;
    protected final ServiceBusNamespace namespace;
    protected final Function<String, String> connectionStringCreator;

    AbstractServiceBusSenderFactory(AzureAdmin azureAdmin, String namespace) {
        this.azureAdmin = azureAdmin;
        this.namespace = azureAdmin
                .getOrCreateServiceBusNamespace(namespace);
        ServiceBusConnectionStringProvider provider = new ServiceBusConnectionStringProvider(this.namespace);
        this.connectionStringCreator = provider::getConnectionString;
    }
}
