/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

/**
 * Operations to access certain Azure Config Service configuration store.
 */
public interface ConfigServiceOperations {
    /**
     * Find a config key-value item with given {@code keyName} and {@link Nullable} {@code label}
     *
     * @param keyName name of the key
     * @param label   label of the key, can be null
     * @return {@link Optional<KeyValueItem>}, if key does not exist, return {@link Optional#empty()}.
     */
    Optional<KeyValueItem> getKey(String keyName, @Nullable String label);

    /**
     * Find all key-value items in current configuration store.
     *
     * @return all key-value items {@link List<KeyValueItem>} in current configuration store, return empty list if
     *         no key found.
     */
    List<KeyValueItem> getAllKeys();

    /**
     * Find all key-value items which key name is prefixed with {@code prefix}, and has {@code label} if provided.
     *
     * @param prefix key name prefix of which keys will be loaded.
     * @param label
     * @return all key-value items {@link List<KeyValueItem>} which match given condition.
     */
    List<KeyValueItem> getKeys(@NotEmpty String prefix, @Nullable String label);
}
