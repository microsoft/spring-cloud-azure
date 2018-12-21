/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.resource;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container for connection string of stores
 */
public class ConnectionStringPool {
    private Map<String, ConnectionString> connectionStringMap = new ConcurrentHashMap<>();

    public void put(String storeName, ConnectionString connectionString) {
        Assert.hasText(storeName, "Config store name cannot be null or empty.");
        Assert.notNull(connectionString, "Connection string should not be null.");
        this.connectionStringMap.put(storeName, connectionString);
    }

    public void put(String storeName, String connectionString) {
        this.put(storeName, ConnectionString.of(connectionString));
    }

    @Nullable
    public ConnectionString get(String storeName) {
        return this.connectionStringMap.get(storeName);
    }

    public Map<String, ConnectionString> getAll() {
        return this.connectionStringMap;
    }
}

