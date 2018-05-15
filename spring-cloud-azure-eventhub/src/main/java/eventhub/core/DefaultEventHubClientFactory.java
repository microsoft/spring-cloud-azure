/*
 *  Copyright 2017-2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eventhub.core;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;

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
            }
            catch (EventHubException | IOException e) {
                throw new EventHubRuntimeException("Error when creating event hub client", e);
            }
        });
    }
}
