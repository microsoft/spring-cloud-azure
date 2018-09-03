/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StorageConnectionStringProvider {

    private static final Function<StorageAccount, String> connectionStringProvider =
            Memoizer.memoize(StorageConnectionStringProvider::buildConnectionString);

    private static String buildConnectionString(StorageAccount storageAccount) {
        return storageAccount.getKeys().stream().findFirst()
                             .map(key -> ConnectionStringBuilder.build(storageAccount.name(), key.value()))
                             .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    public static String getConnectionString(StorageAccount storageAccount) {
        return connectionStringProvider.apply(storageAccount);
    }

    private static class ConnectionStringBuilder {
        private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

        private static final String ACCOUNT_NAME = "AccountName";

        private static final String ACCOUNT_KEY = "AccountKey";

        private static final String ENDPOINT_SUFFIX = "EndpointSuffix";

        private static final String HTTP_PROTOCOL = "http";

        private static final String DEFAULT_ENDPOINT_SUFFIX = "core.windows.net";

        private static final String SEPARATOR = ";";

        static String build(String accountName, String accountKey) {
            Map<String, String> map = new HashMap<>();
            map.put(DEFAULT_PROTOCOL, HTTP_PROTOCOL);
            map.put(ACCOUNT_NAME, accountName);
            map.put(ACCOUNT_KEY, accountKey);
            map.put(ENDPOINT_SUFFIX, DEFAULT_ENDPOINT_SUFFIX);

            return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
        }
    }
}
