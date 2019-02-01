/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AzureConfigPropertySource extends EnumerablePropertySource<ConfigServiceOperations> {
    private final String context;
    private Map<String, Object> properties = new LinkedHashMap<>();
    private final String storeName;
    private final String label;

    public AzureConfigPropertySource(String context, ConfigServiceOperations operations, String storeName,
                                     String label) {
        // The context alone does not uniquely define a PropertySource, append storeName and label to uniquely
        // define a PropertySource
        super(context + storeName + "/" + label, operations);
        this.context = context;
        this.storeName = storeName;
        this.label = label;
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
        // * for wildcard match
        QueryOptions queryOptions = new QueryOptions().withKeyNames(context + "*").withLabels(label);
        List<KeyValueItem> items = source.getKeys(storeName, queryOptions);

        for (KeyValueItem item : items) {
            String key = item.getKey().trim().substring(context.length()).replace('/', '.');
            properties.put(key, item.getValue());
        }
    }
}
