/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.storage;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.api.Environment;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;

import java.util.function.BiFunction;

public class StorageConnectionStringProvider {

    private static final BiFunction<StorageAccount, Environment, String> connectionStringProvider =
            Memoizer.memoize(StorageConnectionStringProvider::buildConnectionString);

    private static String buildConnectionString(StorageAccount storageAccount, Environment environment) {
        return storageAccount.getKeys().stream().findFirst().map(key -> StorageConnectionStringBuilder
                .build(storageAccount.name(), key.value(), environment))
                             .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    public static String getConnectionString(StorageAccount storageAccount, Environment environment) {
        return connectionStringProvider.apply(storageAccount, environment);
    }

    public static String getConnectionString(String storageAccount, String accessKey, Environment environment) {
        return StorageConnectionStringBuilder.build(storageAccount, accessKey, environment);
    }
}
