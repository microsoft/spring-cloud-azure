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
    protected String destination = "event-hub";
    private Consumer<D> consumer = this::handleMessage;

    @Test
    public void testSubscribe() {
        boolean succeed = this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, consumerGroup);

        assertTrue(succeed);

        verifySubscriberCreatorCalled(1);
        verifySubscriberRegistered(1);
    }

    @Test
    public void testSubscribeTwice() {
        boolean onceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumer, consumerGroup);

        assertTrue(onceSucceed);
        verifySubscriberRegistered(1);

        boolean twiceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumer, consumerGroup);

        assertFalse(twiceSucceed);

        verifySubscriberCreatorCalled(1);
        verifySubscriberRegistered(1);
    }

    @Test
    public void testSubscribeWithAnotherGroup() {
        boolean onceSucceed = this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, consumerGroup);

        assertTrue(onceSucceed);
        verifySubscriberRegistered(1);

        boolean twiceSucceed =
                this.subscribeByGroupOperation.subscribe(destination, this::handleMessage, anotherConsumerGroup);

        assertTrue(twiceSucceed);

        verifySubscriberCreatorCalled(2);
    }

    @Test
    public void testUnsubscribeNotSubscribed() {
        boolean unsubscribed = this.subscribeByGroupOperation.unsubscribe(destination, consumerGroup);

        assertFalse(unsubscribed);

        verifySubscriberCreatorCalled(0);
    }

    private void handleMessage(D event) {
    }

    private void handleMessageAnother(D event) {
    }

    protected abstract void verifySubscriberCreatorCalled(int times);

    protected abstract void verifySubscriberRegistered(int times);
}
