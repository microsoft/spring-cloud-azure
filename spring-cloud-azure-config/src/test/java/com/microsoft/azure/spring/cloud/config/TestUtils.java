/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import java.util.List;

/**
 * Utility methods which can be used across different test classes
 */
public class TestUtils {
    private TestUtils() {
    }

    static String propPair(String propName, String propValue) {
        return String.format("%s=%s", propName, propValue);
    }

    static KeyValueItem createItem(String context, String key, String value) {
        KeyValueItem item = new KeyValueItem();
        item.setKey(context + key);
        item.setValue(value);

        return item;
    }

    static void addStore(AzureCloudConfigProperties properties, String storeName, String connectionString) {
        List<ConfigStore> stores = properties.getStores();
        ConfigStore store = new ConfigStore();
        store.setConnectionString(connectionString);
        store.setName(storeName);
        stores.add(store);
        properties.setStores(stores);
    }
}
