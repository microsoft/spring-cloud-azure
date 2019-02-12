/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.integration.servicebus;

/**
 * Service bus client related config
 *
 * @author Warren Zhu
 */
public class ServiceBusClientConfig {

    private final int prefetchCount;

    private final int concurrency;

    private ServiceBusClientConfig(int prefetchCount, int concurrency) {
        this.prefetchCount = prefetchCount;
        this.concurrency = concurrency;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public static ServiceBusClientConfigBuilder builder(){
        return new ServiceBusClientConfigBuilder();
    }

    public static class ServiceBusClientConfigBuilder {
        private int prefetchCount = 1;
        private int concurrency = 1;

        public ServiceBusClientConfigBuilder setPrefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
            return this;
        }

        public ServiceBusClientConfigBuilder setConcurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        public ServiceBusClientConfig build() {
            return new ServiceBusClientConfig(prefetchCount, concurrency);
        }
    }
}
