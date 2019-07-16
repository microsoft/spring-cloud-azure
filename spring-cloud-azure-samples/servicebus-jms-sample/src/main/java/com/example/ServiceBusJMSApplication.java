/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

//package com.example;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.jms.annotation.EnableJms;
//import org.springframework.jms.core.JmsTemplate;
//
//@SpringBootApplication
//@EnableJms
//public class ServiceBusJMSApplication {
//
//    private static final String QUEUE_NAME = "que001";
//
//    private static final String USER_NAME = "usr001";
//
//    private static final Logger logger = LoggerFactory.getLogger(ServiceBusJMSApplication.class);
//
//    static ConfigurableApplicationContext context;
//
//    @Autowired
//    private static JmsTemplate jmsTemplate;
//
//    public static void main(String[] args) {
//
//        context = SpringApplication.run(ServiceBusJMSApplication.class, args);
//
//        jmsTemplate = context.getBean(JmsTemplate.class);
//
//        logger.info("Sending message");
//
//        jmsTemplate.convertAndSend(QUEUE_NAME, new User(USER_NAME));
//
//    }
//
//}

package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
@EnableJms
public class ServiceBusJMSApplication {

    static ConfigurableApplicationContext context;

    public static void main(String[] args) {

        context = SpringApplication.run(ServiceBusJMSApplication.class, args);

    }

}
