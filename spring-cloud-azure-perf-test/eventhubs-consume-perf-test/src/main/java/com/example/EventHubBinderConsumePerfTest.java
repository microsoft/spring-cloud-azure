/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import com.microsoft.azure.servicebus.stream.binder.test.ConsumerStatistics;
import com.microsoft.azure.servicebus.stream.binder.test.ProducerPerformance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;

import java.util.concurrent.CountDownLatch;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableBinding(Sink.class)
public class EventHubBinderConsumePerfTest implements CommandLineRunner {

    @Autowired
    Sink sink;

    private CountDownLatch testCompleted = new CountDownLatch(1);

    private final ConsumerStatistics consumerStatistics = new ConsumerStatistics(1000, 5000, this.testCompleted);

    public static void main(String[] args) {
        SpringApplication.run(EventHubBinderConsumePerfTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        testCompleted.await();
        System.exit(0);
    }

    @StreamListener(Sink.INPUT)
    public void handleMessage(byte[] payload)  {
        consumerStatistics.record(payload.length);
    }
}
