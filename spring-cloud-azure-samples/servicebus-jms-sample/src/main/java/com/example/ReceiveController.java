/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveController {

    private static final String QUEUE_NAME = "que001";

    private final Logger logger = LoggerFactory.getLogger(ReceiveController.class);

    private static String content;

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(Email email) {

        logger.info("Receiving message from queue: {}", email);

        content = email.getContent();

        System.out.printf("New message received: '%s'", email.getContent());

    }

    public static String getContent() {
        return content;
    }

}
