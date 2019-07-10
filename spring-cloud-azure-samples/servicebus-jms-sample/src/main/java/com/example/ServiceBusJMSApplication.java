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
    // Number of messages to send
    private static int totalSend = 10;
    // log4j logger
    private static final Logger logger = LoggerFactory.getLogger(ServiceBusJMSApplication.class);

    public static void main(String[] args) {
        // Launch the application
        ConfigurableApplicationContext context = SpringApplication.run(ServiceBusJMSApplication.class, args);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        logger.info("Sending message");

        // Send messages to the queue
        for (int i = 0; i < totalSend; i++) {
            System.out.printf("Sending message %d.\n", i + 1);
            jmsTemplate.convertAndSend("que001", new EmailController("info@example.com", "Hello"));
        }

//        // Send messages to the topic
//        for (int i = 0; i < totalSend; i++) {
//            System.out.printf("Sending message %d.\n", i + 1);
//            jmsTemplate.convertAndSend("testtopic", new Email("info@example.com", "Hello"));
//        }

    }

}
