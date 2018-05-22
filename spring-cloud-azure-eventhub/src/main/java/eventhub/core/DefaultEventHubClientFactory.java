/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class DefaultEventHubClientFactory implements EventHubClientFactory {
    private final ConcurrentHashMap<String, EventHubClient> clients = new ConcurrentHashMap<>();

    @Override
    public EventHubClient createEventHubClient(String eventHubName) {
        return this.clients.computeIfAbsent(eventHubName, key -> {
            //TODO: figure out where to get properties to build connection stringq
            ConnectionStringBuilder builder = new ConnectionStringBuilder()
                    .setNamespaceName("----ServiceBusNamespaceName-----")
                    .setEventHubName(eventHubName)
                    .setSasKeyName("-----SharedAccessSignatureKeyName-----")
                    .setSasKey("---SharedAccessSignatureKey----");

            try {
                return EventHubClient.createSync(builder.toString(), Executors.newSingleThreadExecutor());
            } catch (EventHubException | IOException e) {
                throw new EventHubRuntimeException("Error when creating event hub client", e);
            }
        });
    }
}
