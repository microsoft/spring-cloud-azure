package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveController {

    // log4j logger
    private final Logger logger = LoggerFactory.getLogger(ReceiveController.class);

    // Queue receiver
    @JmsListener(destination = "que001", containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(EmailController email) {
        logger.info("Receiving message from queue: {}", email);
        System.out.println("Received <" + email + ">");
    }

//    // Topic receiver
//    @JmsListener(destination = "testtopic", containerFactory = "topicJmsListenerContainerFactory", subscription = "mysubscription")
//    public void receiveTopicMessage(Email email) {
//        logger.info("Receiving message from topic: {}", email);
//        System.out.println("Received <" + email + ">");
//    }

}
