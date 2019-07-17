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

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(User user) {

        logger.info("Receiving message from: {}", user.getName());

    }

}
