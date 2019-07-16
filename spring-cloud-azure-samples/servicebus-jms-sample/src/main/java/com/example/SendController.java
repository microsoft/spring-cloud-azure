package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@EnableJms
public class SendController {

    private static final String QUEUE_NAME = "que001";

    private static final String USER_NAME = "user001";

    private static final Logger logger = LoggerFactory.getLogger(SendController.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping("/messages")
    public String postMessage(@RequestParam String message) {

        jmsTemplate = ServiceBusJMSApplication.context.getBean(JmsTemplate.class);

        logger.info("Sending message");

        jmsTemplate.convertAndSend(QUEUE_NAME, new User(USER_NAME));

        return message;
    }
}
