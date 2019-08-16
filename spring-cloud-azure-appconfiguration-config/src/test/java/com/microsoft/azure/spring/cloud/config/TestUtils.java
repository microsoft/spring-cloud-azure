/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.List;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

/**
 * Utility methods which can be used across different test classes
 */
public class TestUtils {
    private TestUtils() {
    }

    static String propPair(String propName, String propValue) {
        return String.format("%s=%s", propName, propValue);
    }

    static KeyValueItem createItem(String context, String key, String value, String label) {
        KeyValueItem item = new KeyValueItem();
        item.setKey(context + key);
        item.setValue(value);
        item.setLabel(label);
        item.setContentType("");

        return item;
    }

    static void addStore(AzureCloudConfigProperties properties, String storeName, String connectionString) {
        addStore(properties, storeName, connectionString, null);
    }

    static void addStore(AzureCloudConfigProperties properties, String storeName, String connectionString,
                         String label) {
        List<ConfigStore> stores = properties.getStores();
        ConfigStore store = new ConfigStore();
        store.setConnectionString(connectionString);
        store.setName(storeName);
        store.setLabel(label);
        stores.add(store);
        properties.setStores(stores);
    }
}
