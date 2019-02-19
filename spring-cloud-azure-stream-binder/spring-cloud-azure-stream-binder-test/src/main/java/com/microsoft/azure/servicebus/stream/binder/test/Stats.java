/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.servicebus.stream.binder.test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Stats {

    private long start;
    private long windowStart;
    private int[] latencies;
    private int sampling;
    private int index;
    private long count;
    private long bytes;
    private int maxLatency;
    private long totalLatency;
    private long windowCount;
    private int windowMaxLatency;
    private long windowTotalLatency;
    private long windowBytes;
    private long reportingInterval;

    public Stats(long numRecords, int reportingInterval) {
        this.start = System.currentTimeMillis();
        this.windowStart = System.currentTimeMillis();
        this.index = 0;
        this.sampling = (int) (numRecords / Math.min(numRecords, 500000));
        this.latencies = new int[(int) (numRecords / this.sampling) + 1];
        this.index = 0;
        this.maxLatency = 0;
        this.totalLatency = 0;
        this.windowCount = 0;
        this.windowMaxLatency = 0;
        this.windowTotalLatency = 0;
        this.windowBytes = 0;
        this.totalLatency = 0;
        this.reportingInterval = reportingInterval;
    }

    private static int[] percentiles(int[] latencies, int count, double... percentiles) {
        int size = Math.min(count, latencies.length);
        Arrays.sort(latencies, 0, size);
        int[] values = new int[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            int index = (int) (percentiles[i] * size);
            values[i] = latencies[index];
        }
        return values;
    }

    public void record(int iter, int latency, int bytes, long time) {
        this.count++;
        this.bytes += bytes;
        this.totalLatency += latency;
        this.maxLatency = Math.max(this.maxLatency, latency);
        this.windowCount++;
        this.windowBytes += bytes;
        this.windowTotalLatency += latency;
        this.windowMaxLatency = Math.max(windowMaxLatency, latency);
        if (iter % this.sampling == 0) {
            this.latencies[index] = latency;
            this.index++;
        }
        /* maybe report the recent perf */
        if (time - windowStart >= reportingInterval) {
            printWindow();
            newWindow();
        }
    }

    public void printWindow() {
        long ellapsed = System.currentTimeMillis() - windowStart;
        double recsPerSec = 1000.0 * windowCount / (double) ellapsed;
        double mbPerSec = 1000.0 * this.windowBytes / (double) ellapsed / (1024.0 * 1024.0);
        System.out.printf("%d records sent, %.1f records/sec (%.4f MB/sec), %.1f ms avg latency, %.1f max latency.%n",
                windowCount, recsPerSec, mbPerSec, windowTotalLatency / (double) windowCount,
                (double) windowMaxLatency);
    }

    public void newWindow() {
        this.windowStart = System.currentTimeMillis();
        this.windowCount = 0;
        this.windowMaxLatency = 0;
        this.windowTotalLatency = 0;
        this.windowBytes = 0;
    }

    public void printTotal() {
        long elapsed = System.currentTimeMillis() - start;
        double recsPerSec = 1000.0 * count / (double) elapsed;
        double mbPerSec = 1000.0 * this.bytes / (double) elapsed / (1024.0 * 1024.0);
        int[] percs = percentiles(this.latencies, index, 0.5, 0.95, 0.99, 0.999);
        System.out
                .printf("%d records sent, %f records/sec (%.2f MB/sec), %.2f ms avg latency, %.2f ms max latency, %d ms 50th, %d ms 95th, %d ms 99th, %d ms 99.9th.%n",
                        count, recsPerSec, mbPerSec, totalLatency / (double) count, (double) maxLatency, percs[0],
                        percs[1], percs[2], percs[3]);
    }

    public void printTotalAsMarkdown() {
        long elapsed = System.currentTimeMillis() - start;
        double recsPerSec = 1000.0 * count / (double) elapsed;
        double mbPerSec = 1000.0 * this.bytes / (double) elapsed / (1024.0 * 1024.0);
        int[] percs = percentiles(this.latencies, index, 0.5, 0.95, 0.99, 0.999);

        System.out.println(
                "Records sent | Records/sec | MB/sec | Avg latency(ms) | Max latency(ms) | 50%(ms) | 95%" + "(ms) | " +
                        "99%(ms) | " + "99.9%(ms)");
        System.out.println(IntStream.range(1, 11).mapToObj((i) -> "|").collect(Collectors.joining(" -- ")));

        System.out.printf("%d | %f | %.4f | %.2f | %.2f | %d | %d | %d | %d", count, recsPerSec, mbPerSec,
                totalLatency / (double) count, (double) maxLatency, percs[0], percs[1], percs[2], percs[3]);
    }
}
