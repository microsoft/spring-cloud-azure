/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);
    private static final String EVENT_HUB_NAME = "eventhub";
    private static final String CONSUMER_GROUP = "$Default";

    @Autowired
    EventHubOperation eventHubOperation;

    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.eventHubOperation.sendAsync(EVENT_HUB_NAME, MessageBuilder.withPayload(message).build());
        return message;
    }

    @PostConstruct
    public void subscribeToEventHub() {
        this.eventHubOperation
                .setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        this.eventHubOperation.subscribe(EVENT_HUB_NAME, CONSUMER_GROUP, this::messageReceiver, String.class);
    }

    private void messageReceiver(Message<?> message) {
        LOGGER.info("Message arrived! Payload: " + message.getPayload());
        Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                LOGGER.info("Message '{}' successfully checkpointed", message.getPayload());
            }
            return null;
        });
    }
}
