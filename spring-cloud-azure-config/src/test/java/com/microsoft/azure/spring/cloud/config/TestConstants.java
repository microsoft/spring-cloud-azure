/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Test constants which can be shared across different test classes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConstants {
    public static final String CONN_STRING_PROP = "spring.cloud.azure.config.connection-string";
    public static final String DEFAULT_CONTEXT_PROP = "spring.cloud.azure.config.default-context";
    public static final String PREFIX_PROP = "spring.cloud.azure.config.prefix";
    public static final String SEPARATOR_PROP = "spring.cloud.azure.config.profile-separator";

    public static final String VALID_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
}
