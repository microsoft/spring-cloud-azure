/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.io.IOException;
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

    public void initProperties() throws IOException {
        // * for wildcard match
        // Reading in Configurations
        QueryOptions queryOptions = new QueryOptions().withKeyNames(context + "*").withLabels(label);
        List<KeyValueItem> items = source.getKeys(storeName, queryOptions);
        for (KeyValueItem item : items) {
            String key = item.getKey().trim().substring(context.length()).replace('/', '.');
            properties.put(key, item.getValue());
        }

        // Reading In Features
        queryOptions = new QueryOptions().withKeyNames("*appconfig*").withLabels(label);
        items = source.getKeys(storeName, queryOptions);
        createFeatureSet(items);
    }
    
    /**
     * Creates a {@code FeatureSet} from a list of {@code KeyValueItem}. 
     * 
     * @param items New items read in from Azure
     * @param propertyCache Cached values where updated values are set.
     * @param date Cache timestamp
     * @throws IOException
     */
    private void createFeatureSet(List<KeyValueItem> items) throws IOException {
        // Reading In Features
        FeatureSet featureSet = new FeatureSet();
        for (KeyValueItem item : items) {
            Object feature = createFeature(item);
            if (feature != null) {
                String key = item.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());
                featureSet.addFeature(key, feature);
            }
        }
        properties.put(FEATURE_MANAGEMENT_KEY, mapper.convertValue(featureSet, LinkedHashMap.class));
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
