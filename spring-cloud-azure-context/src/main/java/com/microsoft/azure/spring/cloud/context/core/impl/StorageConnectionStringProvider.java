/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.api.Region;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class StorageConnectionStringProvider {

    private static final BiFunction<StorageAccount, Region, String> connectionStringProvider =
            Memoizer.memoize(StorageConnectionStringProvider::buildConnectionString);

    private static String buildConnectionString(StorageAccount storageAccount, Region region) {
        return storageAccount.getKeys().stream().findFirst()
                             .map(key -> ConnectionStringBuilder.build(storageAccount.name(), key.value(), region))
                             .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    public static String getConnectionString(StorageAccount storageAccount, Region region) {
        return connectionStringProvider.apply(storageAccount, region);
    }

    private static class ConnectionStringBuilder {
        private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

        private static final String ACCOUNT_NAME = "AccountName";

        private static final String ACCOUNT_KEY = "AccountKey";

        private static final String ENDPOINT_SUFFIX = "EndpointSuffix";

        private static final String HTTP_PROTOCOL = "http";

        private static final String SEPARATOR = ";";

        static String build(String accountName, String accountKey, Region region) {
            Map<String, String> map = new HashMap<>();
            map.put(DEFAULT_PROTOCOL, HTTP_PROTOCOL);
            map.put(ACCOUNT_NAME, accountName);
            map.put(ACCOUNT_KEY, accountKey);
            map.put(ENDPOINT_SUFFIX, region.getStorageEndpoint());

            return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
        }
    }
}
