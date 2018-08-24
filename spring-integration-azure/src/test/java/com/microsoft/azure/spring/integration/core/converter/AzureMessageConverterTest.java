/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.converter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import static org.junit.Assert.assertEquals;

public abstract class AzureMessageConverterTest<T> {
    protected String payload = "payload";
    private AzureMessageConverter<T> converter;
    private Class<T> targetClass;

    @Before
    public void setUp() {
        converter = getConverter();
        targetClass = getTargetClass();
    }

    @Test
    public void payloadAsString() {
        convertAndBack(payload, String.class);
    }

    @Test
    public void payloadAsByte() {
        convertAndBack(payload.getBytes(), byte[].class);
    }

    @Test
    public void payloadAsTargetType() {
        convertAndBack(getInstance(), targetClass);
    }

    @Test
    public void payloadAsUserClass() {
        convertAndBack(new User(payload), User.class);
    }

    private <U> void convertAndBack(U payload, Class<U> payloadClass) {
        Message<U> message = MessageBuilder.withPayload(payload).build();
        T azureMessage = converter.fromMessage(message, targetClass);
        Message<U> convertedMessage = converter.toMessage(azureMessage, payloadClass);
        assertEquals(convertedMessage.getPayload(), payload);
    }

    protected abstract T getInstance();

    protected abstract AzureMessageConverter<T> getConverter();

    protected abstract Class<T> getTargetClass();
}
