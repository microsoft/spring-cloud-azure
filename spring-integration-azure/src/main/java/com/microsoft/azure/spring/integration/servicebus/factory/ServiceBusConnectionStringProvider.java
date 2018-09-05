/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import lombok.NonNull;

import java.util.function.Function;

class ServiceBusConnectionStringProvider {
    private final Function<String, String> connectionStringProvider = Memoizer.memoize(this::buildConnectionString);

    private final AzureAdmin azureAdmin;

    ServiceBusConnectionStringProvider(@NonNull AzureAdmin azureAdmin) {
        this.azureAdmin = azureAdmin;
    }

    private String buildConnectionString(String namespace) {
        return azureAdmin.getOrCreateServiceBusNamespace(namespace).authorizationRules().list().stream().findFirst()
                                  .map(com.microsoft.azure.management.servicebus.AuthorizationRule::getKeys)
                                  .map(AuthorizationKeys::primaryConnectionString)
                                  .map(s -> new com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder(s,
                                          namespace).toString()).orElseThrow(() -> new ServiceBusRuntimeException(
                        String.format("Service bus namespace '%s' key is empty", namespace), null));
    }

    public String getConnectionString(String namespace) {
        return connectionStringProvider.apply(namespace);
    }
}
