/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Operations to access certain Azure Config Service configuration store.
 */
public interface ConfigServiceOperations {
    /**
     * Find all key-value items which key name is prefixed with {@code prefix}, and has {@code label} if provided.
     *
     * @param prefix key name prefix of which keys will be loaded, is {@link Nullable}.
     * @param store the configuration for the config store from which to get keys
     * @return all key-value items {@link List<KeyValueItem>} which match given condition.
     */
    List<KeyValueItem> getKeys(@Nullable String prefix, @NonNull ConfigStore store);
}
