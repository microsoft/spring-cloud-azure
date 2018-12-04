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
    public static final String SUBSCRIPTION_ID_PROP = "spring.cloud.azure.config.msi.subscription-id";
    public static final String RESOURCE_GROUP_PROP = "spring.cloud.azure.config.msi.resource-group-name";
    public static final String CONFIG_STORE_PROP = "spring.cloud.azure.config.msi.config-store-name";

    public static final String TEST_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    public static final String TEST_ENDPOINT = "https://fake.test.config.io";
    public static final String TEST_KV_API = TEST_ENDPOINT + "/kv?key=fake-key*&label=fake-label";
    public static final String TEST_ID = "fake-conn-id";
    public static final String TEST_SECRET = "ZmFrZS1jb25uLXNlY3JldA=="; // Base64 encoded from fake-conn-secret

    public static final String TEST_SUBSCRIPTION_ID = "fake-subscription-id";
    public static final String TEST_RESOURCE_GROUP = "fake-resource-group";
    public static final String TEST_CONFIG_STORE = "fake-config-store";

    public static final String MSI_TOKEN = "fake_token";

    public static final String TEST_CONTEXT = "/foo/";
    public static final String TEST_KEY_1 = "test_key_1";
    public static final String TEST_VALUE_1 = "test_value_1";
    public static final String TEST_KEY_2 = "test_key_2";
    public static final String TEST_VALUE_2 = "test_value_2";
    public static final String TEST_KEY_3 = "test_key_3";
    public static final String TEST_VALUE_3 = "test_value_3";
}
