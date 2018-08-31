/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder.properties;

import com.microsoft.azure.spring.integration.core.api.StartPosition;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Warren Zhu
 */
@Getter
@Setter
public class EventHubConsumerProperties {
    private StartPosition startPosition = StartPosition.LATEST;
}
