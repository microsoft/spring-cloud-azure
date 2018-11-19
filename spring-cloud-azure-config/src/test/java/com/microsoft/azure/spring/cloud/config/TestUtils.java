/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility methods which can be used across different test classes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    static String propPair(String propName, String propValue) {
        return String.format("%s=%s", propName, propValue);
    }

    static KeyValueItem createItem(String context, String key, String value) {
        KeyValueItem item = new KeyValueItem();
        item.setKey(context + key);
        item.setValue(value);

        return item;
    }
}
