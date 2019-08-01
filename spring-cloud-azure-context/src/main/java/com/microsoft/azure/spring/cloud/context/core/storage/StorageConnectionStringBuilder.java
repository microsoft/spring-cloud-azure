/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.context.core.storage;

import com.microsoft.azure.AzureEnvironment;

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

    public static String build(String accountName, String accountKey, AzureEnvironment environment) {
        Map<String, String> map = new HashMap<>();
        map.put(DEFAULT_PROTOCOL, HTTP_PROTOCOL);
        map.put(ACCOUNT_NAME, accountName);
        map.put(ACCOUNT_KEY, accountKey);
        // Remove starting dot since AzureEnvironment.storageEndpointSuffix() starts with dot
        map.put(ENDPOINT_SUFFIX, environment.storageEndpointSuffix().substring(1));

        return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
    }
}
