/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.spring.integration.SendSubscribeOperationTest;
import com.microsoft.azure.spring.integration.core.support.ServiceBusTopicTestOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.junit.Before;

public class ServiceBusTopicOperationSendSubscribeTest extends SendSubscribeOperationTest<ServiceBusTopicOperation> {

    @Before
    @Override
    public void setUp() {
        this.sendSubscribeOperation = new ServiceBusTopicTestOperation();
    }
}
