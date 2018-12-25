/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AzureConfigPropertySource extends EnumerablePropertySource<ConfigServiceOperations> {
    private final String context;
    private Map<String, Object> properties = new LinkedHashMap<>();
    private final ConfigStore configStore;

    public AzureConfigPropertySource(String context, ConfigServiceOperations operations, ConfigStore configStore) {
        super(context, operations);
        this.context = context;
        this.configStore = configStore;
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> keySet = properties.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void initProperties() {
        String label = this.configStore.getLabel();
        // * for wildcard match
        List<KeyValueItem> items = source.getKeys(context + "*", configStore);

        for (KeyValueItem item : items) {
            String key = item.getKey().trim().substring(context.length()).replace('/', '.');
            properties.put(key, item.getValue());
        }
    }
}
