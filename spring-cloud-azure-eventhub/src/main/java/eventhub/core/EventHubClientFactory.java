/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.microsoft.azure.eventhubs.EventHubClient;

public interface EventHubClientFactory {

    EventHubClient createEventHubClient(String eventHubName);
}
