/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder.test;

import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Consumer perf test via {@link SubscribableChannel}
 *
 * @author Warren Zhu
 */
public class ConsumerPerformance {

    private static final long reportingInterval = 1000L;

    public static void startPerfTest(SubscribableChannel channel) {

        AtomicLong lastReportTime = new AtomicLong(System.currentTimeMillis());
        LongAdder totalMessageRead = new LongAdder();
        AtomicLong lastMessageRead = new AtomicLong();
        LongAdder totalBytesRead = new LongAdder();
        AtomicLong lastBytesRead = new AtomicLong();
        printHeader();

        channel.subscribe((Message<?> message) -> {
            totalMessageRead.increment();
            totalBytesRead.add(((byte[]) message.getPayload()).length);

            long now = System.currentTimeMillis();
            if (now - lastReportTime.get() > reportingInterval) {
                printBasicProgress(lastReportTime.get(), now, totalBytesRead.longValue(), lastBytesRead.longValue(),
                        totalMessageRead.longValue(), lastMessageRead.longValue());

                lastReportTime.set(now);
                lastBytesRead.set(totalBytesRead.longValue());
                lastMessageRead.set(totalMessageRead.longValue());
            }
        });
    }

    private static void printBasicProgress(long startMs, long endMs, long bytesRead, long lastBytesRead,
            long messagesRead, long lastMessagesRead) {
        long elapsedMs = endMs - startMs;
        double totalKbRead = (bytesRead * 1.0) / (1024);
        double intervalKbRead = ((bytesRead - lastBytesRead) * 1.0) / (1024);
        double intervalKbPerSec = 1000.0 * intervalKbRead / elapsedMs;
        double intervalMessagesPerSec = ((messagesRead - lastMessagesRead) * 1.0 / elapsedMs) * 1000.0;
        System.out.println(String.format("%s, %s, %.4f, %.4f, %d, %.4f", startMs, endMs, totalKbRead, intervalKbPerSec,
                messagesRead, intervalMessagesPerSec));
    }

    private static void printHeader() {
        System.out.println("start.time, end.time, data.consumed.in.KB, KB.sec, data.consumed.in.nMsg, nMsg.sec");
    }
}
