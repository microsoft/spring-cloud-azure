/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.context.core.storage;

import com.microsoft.azure.spring.cloud.context.core.api.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class StorageConnectionStringBuilder {
    private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

    private static final String ACCOUNT_NAME = "AccountName";

    private static final String ACCOUNT_KEY = "AccountKey";

    private static final String ENDPOINT_SUFFIX = "EndpointSuffix";

    private static final String HTTP_PROTOCOL = "https";

    private static final String SEPARATOR = ";";

    public static String build(String accountName, String accountKey, Environment environment) {
        Map<String, String> map = new HashMap<>();
        map.put(DEFAULT_PROTOCOL, HTTP_PROTOCOL);
        map.put(ACCOUNT_NAME, accountName);
        map.put(ACCOUNT_KEY, accountKey);
        map.put(ENDPOINT_SUFFIX, environment.getStorageEndpoint());

        return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
    }
}
