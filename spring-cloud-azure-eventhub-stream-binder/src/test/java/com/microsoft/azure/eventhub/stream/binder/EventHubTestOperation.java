/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.support.InMemoryOperation;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.converter.EventHubMessageConverter;

class EventHubTestOperation extends InMemoryOperation<EventData> implements EventHubOperation {

    EventHubTestOperation() {
        super(EventData.class, new EventHubMessageConverter());
    }
}

