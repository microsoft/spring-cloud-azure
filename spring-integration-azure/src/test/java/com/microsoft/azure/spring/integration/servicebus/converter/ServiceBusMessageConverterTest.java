/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.converter;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverterTest;

public class ServiceBusMessageConverterTest extends AzureMessageConverterTest<IMessage> {
    @Override
    protected IMessage getInstance() {
        return new Message(this.payload.getBytes());
    }

    @Override
    public AzureMessageConverter<IMessage> getConverter() {
        return new ServiceBusMessageConverter();
    }

    @Override
    protected Class<IMessage> getTargetClass() {
        return IMessage.class;
    }
}
