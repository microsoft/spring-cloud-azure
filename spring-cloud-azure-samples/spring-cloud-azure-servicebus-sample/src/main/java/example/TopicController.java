/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
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
public class TopicController {

    private static final Log LOGGER = LogFactory.getLog(TopicController.class);
    private static final String TOPIC_NAME = "topic";
    private static final String SUBSCRIPTION_NAME = "group1";

    @Autowired
    ServiceBusTopicOperation topicOperation;

    @PostMapping("/topics")
    public String send(@RequestParam("message") String message) {
        this.topicOperation.sendAsync(TOPIC_NAME, MessageBuilder.withPayload(message).build());
        return message;
    }

    @PostConstruct
    public void subscribe(){
        this.topicOperation.subscribe(TOPIC_NAME, SUBSCRIPTION_NAME, this::messageReceiver, String.class);
    }

    private void messageReceiver(Message<?> message) {
        LOGGER.info("Message arrived! Payload: " + message.getPayload());
    }
}
