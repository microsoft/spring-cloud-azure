/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility methods which can be used across different test classes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static String propPair(String propName, String propValue) {
        return String.format("%s=%s", propName, propValue);
    }
}
