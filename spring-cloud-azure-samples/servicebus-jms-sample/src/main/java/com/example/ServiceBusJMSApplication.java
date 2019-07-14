package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
@EnableJms
public class ServiceBusJMSApplication {

    private static final String QUEUE_NAME = "que001";

    private static final String DESTINATION = "user@example.com";

    private static final String CONTENT = "hello";

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusJMSApplication.class);

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(ServiceBusJMSApplication.class, args);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        logger.info("Sending message");

        System.out.printf("Sending message.\n");

        jmsTemplate.convertAndSend(QUEUE_NAME, new EmailController(DESTINATION, CONTENT));

    }

}
