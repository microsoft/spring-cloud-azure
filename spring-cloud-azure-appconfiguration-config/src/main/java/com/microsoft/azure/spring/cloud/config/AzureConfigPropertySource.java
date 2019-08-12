/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.Feature;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureManagementItem;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureSet;

public class AzureConfigPropertySource extends EnumerablePropertySource<ConfigServiceOperations> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigPropertySource.class);

    private final String context;

    private Map<String, Object> properties = new LinkedHashMap<>();

    private final String storeName;

    private final String label;

    private AzureCloudConfigProperties azureProperties;

    private static ObjectMapper mapper = new ObjectMapper();

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    public AzureConfigPropertySource(String context, ConfigServiceOperations operations, String storeName,
            String label, AzureCloudConfigProperties azureProperties) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely
        // define a PropertySource
        super(context + storeName + "/" + label, operations);
        this.context = context;
        this.storeName = storeName;
        this.label = label;
        this.azureProperties = azureProperties;
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

    public void initProperties(PropertyCache propertyCache) throws IOException {
        Date date = new Date();
        if (propertyCache.getContext(storeName) == null) {
            propertyCache.addContext(storeName, context);
            // * for wildcard match
            QueryOptions queryOptions = new QueryOptions().withKeyNames(context + "*").withLabels(label);
            List<KeyValueItem> items = source.getKeys(storeName, queryOptions);
            for (KeyValueItem item : items) {
                String key = item.getKey().trim().substring(context.length()).replace('/', '.');
                properties.put(key, item.getValue());
            }
            propertyCache.addKeyValuesToCache(items, storeName, date);

            // Reading In Features

            queryOptions = new QueryOptions().withKeyNames("*appconfig*").withLabels(label);

            items = source.getKeys(storeName, queryOptions);

            FeatureSet featureSet = new FeatureSet();

            for (KeyValueItem item : items) {
                if (item.getContentType().equals(FEATURE_FLAG_CONTENT_TYPE)) {
                    try {
                        FeatureManagementItem featureItem = mapper.readValue(item.getValue(),
                                FeatureManagementItem.class);
                        Feature feature = new Feature();
                        feature.setEnabled(featureItem.getEnabled());
                        feature.setId(featureItem.getId());
                        feature.setEnabledFor(featureItem.getConditions().getClientFilters());
                        featureSet.addFeature(feature);
                    } catch (IOException e) {
                        LOGGER.error("Unabled to parse Feature Management values from Azure.", e);
                        if (azureProperties.isFailFast()) {
                            throw e;
                        }
                    }
                } else {
                    LOGGER.error(String.format("Found Feature Flag %s with invalid Content Type of %s", item.getKey(),
                            item.getContentType()));
                    if (azureProperties.isFailFast()) {
                        throw new IOException();
                    }
                }
            }
            LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSet, LinkedHashMap.class);
            properties.put(FEATURE_MANAGEMENT_KEY, convertedValue);
            propertyCache.addKeyValuesToCache(items, storeName, date);
        } else {
            if (propertyCache.getRefreshKeys(storeName) != null &&
                    propertyCache.getRefreshKeys(storeName).size() > 0) {
                for (String refreshKey : propertyCache.getRefreshKeys(storeName)) {
                    QueryOptions queryOptions = new QueryOptions().withKeyNames(refreshKey).withLabels(label);
                    List<KeyValueItem> items = source.getKeys(storeName, queryOptions);
                    propertyCache.addKeyValuesToCache(items, storeName, date);
                }
            }

            for (String key : propertyCache.getKeySet(storeName)) {
                if (key.startsWith(context)) {
                    String cachedKey = key.trim().substring(context.length()).replace('/', '.');
                    properties.put(cachedKey, propertyCache.getCachedValue(key));
                } else {
                    List<KeyValueItem> items = new ArrayList<KeyValueItem>();
                    KeyValueItem item = new KeyValueItem();
                    item.setKey(key);
                    item.setValue(propertyCache.getCachedValue(key));
                    item.setContentType(FEATURE_FLAG_CONTENT_TYPE);
                    items.add(item);

                    FeatureSet featureSet = createFeatureSet(items);
                    // LinkedHashMap<?, ?> convertedValue =
                    // mapper.convertValue(featureSet, LinkedHashMap.class);
                    // Adding Features to Cache
                    properties.put(FEATURE_MANAGEMENT_KEY, featureSet);
                }
            }
        }
    }

    private FeatureSet createFeatureSet(List<KeyValueItem> items) throws IOException {
        // Reading In Features
        FeatureSet featureSet = new FeatureSet();
        for (KeyValueItem item : items) {
            Feature feature = createFeature(item);
            if (feature != null) {
                featureSet.addFeature(feature);
            }
        }
        return featureSet;
    }

    private Feature createFeature(KeyValueItem item) throws IOException {
        Feature feature = null;
        if (item.getContentType().equals(FEATURE_FLAG_CONTENT_TYPE)) {
            try {
                FeatureManagementItem featureItem = mapper.readValue(item.getValue(), FeatureManagementItem.class);
                return new Feature(featureItem);

            } catch (IOException e) {
                LOGGER.error("Unabled to parse Feature Management values from Azure.", e);
                if (azureProperties.isFailFast()) {
                    throw e;
                }
            }

        } else {
            LOGGER.error(String.format("Found Feature Flag %s with invalid Content Type of %s", item.getKey(),
                    item.getContentType()));
            if (azureProperties.isFailFast()) {
                throw new IOException();
            }
        }
        return feature;
    }
}
