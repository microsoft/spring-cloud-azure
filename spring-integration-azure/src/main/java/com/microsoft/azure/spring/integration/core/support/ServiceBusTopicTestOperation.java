/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.support;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;

public class ServiceBusTopicTestOperation extends InMemoryOperation<IMessage> implements ServiceBusTopicOperation {
    public ServiceBusTopicTestOperation() {
        super(IMessage.class, new ServiceBusMessageConverter());
    }
}

