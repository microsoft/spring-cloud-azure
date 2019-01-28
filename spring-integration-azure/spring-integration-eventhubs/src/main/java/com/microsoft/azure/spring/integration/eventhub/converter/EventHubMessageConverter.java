/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.converter;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.springframework.messaging.Message;

import java.nio.charset.Charset;

/**
 * A converter to turn a {@link Message} to {@link EventData} and vice versa.
 *
 * @author Warren Zhu
 */
public class EventHubMessageConverter extends AbstractAzureMessageConverter<EventData> {

    @Override
    protected byte[] getPayload(EventData azureMessage) {
        return azureMessage.getBytes();
    }

    @Override
    protected EventData fromString(String payload) {
        return EventData.create(payload.getBytes(Charset.defaultCharset()));
    }

    @Override
    protected EventData fromByte(byte[] payload) {
        return EventData.create(payload);
    }
}
