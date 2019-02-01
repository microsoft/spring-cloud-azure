/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Operations to access certain Azure Config Service configuration store.
 */
public interface ConfigServiceOperations {
    /**
     * Find all key-value items which key name is prefixed with {@code keyPattern} for given {@code store}.
     *
     * @param storeName the store name against which to query
     * @param queryOptions the options used to query the keys
     * @return all key-value items {@link List< KeyValueItem >} which match given condition, result is sorted by the
     * order of the labels defined in {@code queryOptions}
     */
    List<KeyValueItem> getKeys(@NonNull String storeName, @NonNull QueryOptions queryOptions);

    /**
     * List chronological/historical representation of KeyValue resource(s)
     * @param storeName the store name against which to query
     * @param queryOptions the options used to query the keys
     * @return all key-value items {@link List< KeyValueItem >} which match given condition
     */
    List<KeyValueItem> getRevisions(@NonNull String storeName, @NonNull QueryOptions queryOptions);
}
