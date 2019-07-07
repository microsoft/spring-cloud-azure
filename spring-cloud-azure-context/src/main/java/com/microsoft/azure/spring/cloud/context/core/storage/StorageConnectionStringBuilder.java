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

public class StorageConnectionStringBuilder {
    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

    private static final String ACCOUNT_NAME = "AccountName";

    private static final String ACCOUNT_KEY = "AccountKey";

    private static final String ENDPOINT_SUFFIX = "EndpointSuffix";

    private static final String SEPARATOR = ";";

    private final boolean isSecureTransfer;

    public StorageConnectionStringBuilder(boolean isSecureTransfer) {
        this.isSecureTransfer = isSecureTransfer;
    }

    public String build(String accountName, String accountKey, Environment environment) {
        Map<String, String> map = new HashMap<>();
        map.put(DEFAULT_PROTOCOL, resolveProtocol(isSecureTransfer));
        map.put(ACCOUNT_NAME, accountName);
        map.put(ACCOUNT_KEY, accountKey);
        map.put(ENDPOINT_SUFFIX, environment.getStorageEndpoint());

        return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
    }
    
    private String resolveProtocol(boolean isSecureTransfer) {
        return isSecureTransfer ? HTTPS : HTTP;
    }
}
