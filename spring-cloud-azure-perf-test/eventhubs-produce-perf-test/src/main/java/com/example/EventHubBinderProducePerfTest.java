/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import com.microsoft.azure.servicebus.stream.binder.test.ProducerPerformance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableBinding(Source.class)
public class EventHubBinderProducePerfTest implements CommandLineRunner {

    @Autowired
    Source source;

    public static void main(String[] args) {
        SpringApplication.run(EventHubBinderProducePerfTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        producerPerfTests();
    }

    public void producerPerfTests() {
        ProducerPerformance.startPerfTest(this.source.output(), 1000, 1000, 1000);
    }
}
