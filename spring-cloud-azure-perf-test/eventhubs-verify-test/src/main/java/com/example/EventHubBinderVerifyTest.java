/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableBinding({Sink.class, Source.class})
public class EventHubBinderVerifyTest implements CommandLineRunner {

    private final static int messageCount = 1000;
    private final Set<Integer> receivied = new ConcurrentSkipListSet<>();

    @Autowired
    Sink sink;
    @Autowired
    Source source;

    public static void main(String[] args) {
        SpringApplication.run(EventHubBinderVerifyTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        IntStream.range(1, messageCount + 1)
                 .forEach(i -> this.source.output().send(MessageBuilder.withPayload(String.valueOf(i)).build()));
    }

    @StreamListener(Sink.INPUT)
    public void handleMessage(String message) {
        receivied.add(Integer.valueOf(message));

        if (receivied.size() >= messageCount) {
            int missingCount = 0;
            for (int i = 1; i <= messageCount; i++) {
                if (!receivied.contains(i)) {
                    missingCount++;
                }
            }

            System.out.printf("Missing message count is %d%n", missingCount);
        }
    }
}
