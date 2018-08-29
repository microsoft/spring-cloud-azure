/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration;

import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import com.microsoft.azure.spring.integration.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public abstract class SendSubscribeOperationTest<T extends SendOperation & SubscribeByGroupOperation> {

    protected T sendSubscribeOperation;

    private String payload = "payload";
    private User user = new User(payload);
    private Map<String, Object> headers = new HashMap<>();
    private Message<String> stringMessage = new GenericMessage<>(payload, headers);

    private Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(), headers);

    private Message<User> userMessage = new GenericMessage<>(user, headers);

    private String destination = "test";
    private String consumerGroup = "group";
    private String anotherConsumerGroup = "group2";

    @Test
    public void testSendString() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::stringHandler, String.class);
        sendSubscribeOperation.subscribe(destination, anotherConsumerGroup, this::byteHandler, byte[].class);
        sendSubscribeOperation.sendAsync(destination, stringMessage);
    }

    @Test
    public void testSendByte() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::stringHandler, String.class);
        sendSubscribeOperation.subscribe(destination, anotherConsumerGroup, this::byteHandler, byte[].class);
        sendSubscribeOperation.sendAsync(destination, byteMessage);
    }

    @Test
    public void testSendUser() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::userHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    private void stringHandler(Message<?> message) {
        assertEquals(payload, message.getPayload());
    }

    private void byteHandler(Message<?> message) {
        assertEquals(payload, new String((byte[]) message.getPayload()));
    }

    private void userHandler(Message<?> message) {
        assertEquals(user, message.getPayload());
    }

    @Before
    public abstract void setUp();
}
