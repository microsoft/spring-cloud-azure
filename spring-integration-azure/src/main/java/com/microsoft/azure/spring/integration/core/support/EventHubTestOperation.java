/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.support;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.converter.EventHubMessageConverter;

public class EventHubTestOperation extends InMemoryOperation<EventData> implements EventHubOperation {
    public EventHubTestOperation() {
        super(EventData.class, new EventHubMessageConverter());
    }
}

