/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration;

import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class SubscribeByGroupOperationTest<D, K, O extends SubscribeByGroupOperation<D, K>> {
    protected O subscribeByGroupOperation;
    protected String consumerGroup = "consumer-group";
    protected String anotherConsumerGroup = "consumer-group2";
    private String destination = "event-hub";
    private Consumer<Iterable<D>> consumer = this::handleMessage;

    @Test
    public void testSubscribeAndUnsubscribe() {
        boolean succeed = this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, consumerGroup);

        assertTrue(succeed);

        verifySubscriberCreatorCalled(1);

        boolean unsubscribed = this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, consumerGroup);

        assertTrue(unsubscribed);
    }

    @Test
    public void testSubscribeTwice() {
        boolean onceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumer, consumerGroup);

        assertTrue(onceSucceed);

        boolean twiceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumer, consumerGroup);

        assertFalse(twiceSucceed);

        verifySubscriberCreatorCalled(1);
    }

    @Test
    public void testSubscribeWithAnotherGroup() {
        boolean onceSucceed = this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, consumerGroup);

        assertTrue(onceSucceed);

        boolean twiceSucceed =
                this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, anotherConsumerGroup);

        assertTrue(twiceSucceed);

        verifySubscriberCreatorCalled(2);
    }

    @Test
    public void testUnsubscribeNotSubscribed() {
        boolean unsubscribed =
                this.subscribeByGroupOperation.unsubscribe(destination, this::handleMessageAnother, consumerGroup);

        assertFalse(unsubscribed);

        verifySubscriberCreatorCalled(0);
    }

    private void handleMessage(Iterable<D> events) {
    }

    private void handleMessageAnother(Iterable<D> events) {
    }

    protected abstract void verifySubscriberCreatorCalled(int times);
}
