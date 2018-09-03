/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import org.springframework.lang.NonNull;

import java.util.function.Function;

class ServiceBusConnectionStringProvider {
    private ServiceBusNamespace serviceBusNamespace;
    private final Function<String, String> connectionStringProvider = Memoizer.memoize(this::buildConnectionString);

    ServiceBusConnectionStringProvider(@NonNull ServiceBusNamespace serviceBusNamespace) {
        this.serviceBusNamespace = serviceBusNamespace;
    }

    private String buildConnectionString(String name) {
        return serviceBusNamespace.authorizationRules().list().stream().findFirst()
                                  .map(com.microsoft.azure.management.servicebus.AuthorizationRule::getKeys)
                                  .map(AuthorizationKeys::primaryConnectionString)
                                  .map(s -> new com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder(s,
                                          name).toString()).orElseThrow(() -> new ServiceBusRuntimeException(
                        String.format("Service bus namespace '%s' key is empty", name), null));
    }

    public String getConnectionString(String eventHub) {
        return connectionStringProvider.apply(eventHub);
    }
}
