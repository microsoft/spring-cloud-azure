/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.api.Region;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;

import java.util.function.BiFunction;

public class StorageConnectionStringProvider {

    private static final BiFunction<StorageAccount, Region, String> connectionStringProvider =
            Memoizer.memoize(StorageConnectionStringProvider::buildConnectionString);

    private static String buildConnectionString(StorageAccount storageAccount, Region region) {
        return storageAccount.getKeys().stream().findFirst().map(key -> StorageConnectionStringBuilder
                .build(storageAccount.name(), key.value(), region))
                             .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    public static String getConnectionString(StorageAccount storageAccount, Region region) {
        return connectionStringProvider.apply(storageAccount, region);
    }

}
