/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.management.servicebus.AuthorizationRule;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.function.Function;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
public abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {
    protected final AzureAdmin azureAdmin;
    protected final ServiceBusNamespace namespace;

    @Getter
    private final Function<String, String> connectionStringCreator = Memoizer.memoize(this::getConnectionString);

    AbstractServiceBusSenderFactory(@NonNull AzureAdmin azureAdmin, @NonNull String namespace) {
        Assert.hasText(namespace, "namespace can't be null or empty");
        this.azureAdmin = azureAdmin;
        this.namespace = azureAdmin.getOrCreateServiceBusNamespace(namespace);
    }

    private String getConnectionString(String name) {
        return namespace.authorizationRules().list().stream().findFirst().map(AuthorizationRule::getKeys)
                        .map(AuthorizationKeys::primaryConnectionString)
                        .map(s -> new ConnectionStringBuilder(s, name).toString()).orElseThrow(
                        () -> new ServiceBusRuntimeException(
                                String.format("Service bus namespace '%s' key is empty", name), null));
    }
}
