/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import com.microsoft.azure.servicebus.stream.binder.test.ConsumerPerformance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableBinding(Sink.class)
public class EventHubBinderConsumePerfTest implements CommandLineRunner {

    @Autowired
    Sink sink;

    public static void main(String[] args) {
        SpringApplication.run(EventHubBinderConsumePerfTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        consumerPerfTests();
    }

    public void consumerPerfTests() {
        ConsumerPerformance.startPerfTest(sink.input());
    }
}
