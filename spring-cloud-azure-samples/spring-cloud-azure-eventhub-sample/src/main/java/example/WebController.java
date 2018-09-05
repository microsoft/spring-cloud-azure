/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOGGER = LogFactory.getLog(WebController.class);
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
    public void subscribeToEventHub(){
        this.eventHubOperation.subscribe(EVENT_HUB_NAME, CONSUMER_GROUP, this::messageReceiver, String.class);
    }

    private void messageReceiver(Message<?> message) {
        LOGGER.info("Message arrived! Payload: " + message.getPayload());
    }
}
