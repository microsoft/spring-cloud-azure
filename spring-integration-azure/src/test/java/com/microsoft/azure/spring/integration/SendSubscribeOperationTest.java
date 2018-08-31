/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.microsoft.azure.spring.integration.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public abstract class SendSubscribeOperationTest<T extends SendOperation & SubscribeByGroupOperation> {

    protected T sendSubscribeOperation;
    protected String partitionId = "1";
    protected String destination = "test";
    protected String consumerGroup = "group";
    private String payload = "payload";
    private User user = new User(payload);
    private Map<String, Object> headers = new HashMap<>();
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

    protected List<Message<User>> messages =
            IntStream.range(1, 5).mapToObj(String::valueOf).map(User::new).map(u -> new GenericMessage<>(u, headers))
                     .collect(Collectors.toList());
    private Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(), headers);
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

    @Test
    public void testSendReceiveWithManualCheckpointMode() {
        sendSubscribeOperation.setCheckpointMode(CheckpointMode.MANUAL);
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::manualCheckpointHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    @Test
    public void testSendReceiveWithRecordCheckpointMode() {
        sendSubscribeOperation.setCheckpointMode(CheckpointMode.RECORD);
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::recordCheckpointHandler, User.class);
        messages.forEach(m -> sendSubscribeOperation.sendAsync(destination, m));
        verifyCheckpointSuccessCalled(messages.size());
    }

    private void manualCheckpointHandler(Message<?> message) {
        assertTrue(message.getHeaders().containsKey(AzureHeaders.CHECKPOINTER));
        Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
        assertNotNull(checkpointer);
        verifyCheckpointSuccess(checkpointer);
        verifyCheckpointFailure(checkpointer);
    }

    private void recordCheckpointHandler(Message<?> message) {
        //
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

    protected abstract void verifyCheckpointSuccessCalled(int times);

    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    protected abstract void verifyCheckpointFailureCalled(int times);

    protected void verifyCheckpointSuccess(Checkpointer checkpointer) {
        checkpointer.success();
        verifyCheckpointSuccessCalled(1);
    }

    protected void verifyCheckpointFailure(Checkpointer checkpointer) {
        checkpointer.failure();
        verifyCheckpointFailureCalled(1);
    }
}
