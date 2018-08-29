/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.spring.integration.SendSubscribeOperationTest;
import com.microsoft.azure.spring.integration.core.support.EventHubTestOperation;
import org.junit.Before;

public class EventHubOperationSendSubscribeTest extends SendSubscribeOperationTest<EventHubOperation> {

    @Before
    @Override
    public void setUp() {
        this.sendSubscribeOperation = new EventHubTestOperation();
    }
}
