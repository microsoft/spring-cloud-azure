/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.factory;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.management.eventhub.AuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationKey;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubRuntimeException;
import org.springframework.lang.NonNull;

import java.util.function.Function;

public class EventHubConnectionStringProvider {
    private EventHubNamespace eventHubNamespace;
    private final Function<String, String> connectionStringProvider = Memoizer.memoize(this::buildConnectionString);

    public EventHubConnectionStringProvider(@NonNull EventHubNamespace eventHubNamespace) {
        this.eventHubNamespace = eventHubNamespace;
    }

    private String buildConnectionString(String eventHub) {
        return eventHubNamespace.listAuthorizationRules().stream().findFirst().map(AuthorizationRule::getKeys)
                                .map(EventHubAuthorizationKey::primaryConnectionString)
                                .map(s -> new ConnectionStringBuilder(s).setEventHubName(eventHub).toString())
                                .orElseThrow(() -> new EventHubRuntimeException(
                                        String.format("Failed to fetch connection string of '%s'", eventHub), null));
    }

    public String getConnectionString(String eventHub) {
        return connectionStringProvider.apply(eventHub);
    }
}
