/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.support.InMemoryOperation;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;

class ServiceBusTopicTestOperation extends InMemoryOperation<IMessage> implements ServiceBusTopicOperation {
    ServiceBusTopicTestOperation() {
        super(IMessage.class, new ServiceBusMessageConverter());
    }
}

