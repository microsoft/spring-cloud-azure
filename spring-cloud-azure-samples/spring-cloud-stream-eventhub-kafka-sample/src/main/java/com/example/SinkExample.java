/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

/**
 * @author Warren Zhu
 */
@EnableBinding(Sink.class)
public class SinkExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SinkExample.class);

    @StreamListener(Sink.INPUT)
    public void handleMessage(UserMessage userMessage) {
            LOGGER.info("New message received: " + userMessage);
    }
}
