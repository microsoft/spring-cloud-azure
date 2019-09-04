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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.ReflectionUtils;

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
    
    private static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    public AzureConfigPropertySource(String context, ConfigServiceOperations operations, String storeName,
            String label, AzureCloudConfigProperties azureProperties) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
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

    /**
     * Gets settings from Azure/Cache to set as configurations. Updates the cache. </br>
     * </br>
     * <b>Note</b>: Doesn't update Feature Management, just stores values in cache. Call
     * {@code initFeatures} to update Feature Management, but make sure its done in the
     * last {@code AzureConfigPropertySource}
     * 
     * @param propertyCache Cached values to use in store. Also contains values the need
     * to be refreshed.
     * @throws IOException
     */
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
            queryOptions = new QueryOptions().withKeyNames(FEATURE_FLAG_PREFIX + "*").withLabels(label);
            items = source.getKeys(storeName, queryOptions);

            createFeatureSet(items, propertyCache, date);

        } else {
            // Using Cached values, first updates cache then sets new properties.
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

                    createFeatureSet(items, propertyCache, date);
                }
            }
        }
    }

    /**
     * Initializes Feature Management configurations. Only one
     * <{@code AzureConfigPropertySource} can call this, and it needs to be done after the
     * rest have run initProperties.
     * @param propertyCache Cached values to use in store. Also contains values the need
     * to be refreshed.
     */
    public void initFeatures(PropertyCache propertyCache) {
        FeatureSet featureSet = new FeatureSet();
        List<String> features = propertyCache.getCache().keySet().stream()
                .filter(key -> key.startsWith(FEATURE_FLAG_PREFIX)).collect(Collectors.toList());
        features.parallelStream().forEach(key -> {
            try {
                featureSet.addFeature(key.trim().substring(FEATURE_FLAG_PREFIX.length()), createFeature(
                        new KeyValueItem(key, propertyCache.getCachedValue(key), FEATURE_FLAG_CONTENT_TYPE)));
            } catch (IOException e) {
                if (azureProperties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
        });

        properties.put(FEATURE_MANAGEMENT_KEY, mapper.convertValue(featureSet, LinkedHashMap.class));
    }

    /**
     * Creates a {@code FeatureSet} from a list of {@code KeyValueItem}. 
     * 
     * @param items New items read in from Azure
     * @param propertyCache Cached values where updated values are set.
     * @param date Cache timestamp
     * @throws IOException
     */
    private void createFeatureSet(List<KeyValueItem> items, PropertyCache propertyCache, Date date) throws IOException {
        // Reading In Features
        FeatureSet featureSet = new FeatureSet();
        for (KeyValueItem item : items) {
            Object feature = createFeature(item);
            if (feature != null) {
                featureSet.addFeature(item.getKey(), feature);
            }
        }
        if (featureSet != null && featureSet.getFeatureManagement() != null) {
            propertyCache.addKeyValuesToCache(items, storeName, date);
        }
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     * 
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     * @throws IOException
     */
    private Object createFeature(KeyValueItem item) throws IOException {
        Feature feature = null;
        if (item.getContentType().equals(FEATURE_FLAG_CONTENT_TYPE)) {
            try {
                FeatureManagementItem featureItem = mapper.readValue(item.getValue(), FeatureManagementItem.class);
                feature = new Feature(featureItem);

                // Setting Enabled For to null, but enabled = true will result in the
                // feature being on. This is the case of a feature is on/off and set to
                // on. This is to tell the difference between conditional/off which looks
                // exactly the same... It should never be the case of Conditional On, and
                // no filters coming from Azure, but it is a valid way from the config
                // file, which should result in false being returned.
                if (feature.getEnabledFor().size() == 0 && featureItem.getEnabled() == true) {
                    return true;
                }
                return feature;

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
