/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;

/**
 * Azure event hub operation to support send data asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface EventHubOperation extends SendOperation<EventData>, SubscribeByGroupOperation<EventData, EventData> {
}
