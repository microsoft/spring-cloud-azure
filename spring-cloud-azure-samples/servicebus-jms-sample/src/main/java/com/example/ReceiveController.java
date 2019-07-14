package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveController {

    private static final String QUEUE_NAME = "que001";

    private final Logger logger = LoggerFactory.getLogger(ReceiveController.class);

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(EmailController email) {

        logger.info("Receiving message from queue: {}", email);

        System.out.printf("New message received: '%s'", email.getContent());

    }

}
