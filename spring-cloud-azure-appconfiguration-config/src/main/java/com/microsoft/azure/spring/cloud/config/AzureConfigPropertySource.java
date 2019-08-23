/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_FLAG_CONTENT_TYPE;
import static com.microsoft.azure.spring.cloud.config.Constants.KEY_VAULT_CONTENT_TYPE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.Feature;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureManagementItem;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;

public class AzureConfigPropertySource extends EnumerablePropertySource<ConfigurationAsyncClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigPropertySource.class);

    private final String context;

    private Map<String, Object> properties = new LinkedHashMap<>();

    private final String storeName;

    private final String label;

    private AzureCloudConfigProperties azureProperties;

    private static ObjectMapper mapper = new ObjectMapper();

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    public AzureConfigPropertySource(String context, String storeName,
            String label, AzureCloudConfigProperties azureProperties) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely
        // define a PropertySource
        super(context + storeName + "/" + label);
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
     * @throws URISyntaxException
     */
    public void initProperties(PropertyCache propertyCache, ClientStore clients)
            throws IOException, URISyntaxException {
        Date date = new Date();
        SettingSelector settingSelector = new SettingSelector();
        if (!label.equals("%00")) {
            settingSelector.labels(label);
        }
        if (propertyCache.getContext(storeName) == null) {
            propertyCache.addContext(storeName, context);
            
            // * for wildcard match
            settingSelector.keys(context + "*");
            
            // Reading in Settings + Key Vault Values
            List<ConfigurationSetting> settings = clients.listSettings(settingSelector, storeName);

            for (ConfigurationSetting item : settings) {
                String key = item.key().trim().substring(context.length()).replace('/', '.');
                if (item.contentType().equals(KEY_VAULT_CONTENT_TYPE)) {
                    getKeyVaultValue(key, item.value(), item.contentType(), clients);
                } else {
                    properties.put(key, item.value());
                }
                propertyCache.addToCache(item, storeName, date);
            }

            // Reading In Features
            settingSelector.keys("*appconfig*");
            
            settings = clients.listSettings(settingSelector, storeName);
            createFeatureSet(settings, propertyCache, date);
        } else {
            // Using Cached values, first updates cache then sets new properties.
            if (propertyCache.hasRefreshKeys(storeName)) {
                for (String refreshKey : propertyCache.getRefreshKeys(storeName)) {
                    settingSelector.keys(refreshKey);
                    
                    List<ConfigurationSetting> settings = clients.listSettings(settingSelector, storeName);
                    propertyCache.addKeyValuesToCache(settings, storeName, date);
                }
            }

            for (String key : propertyCache.getKeySet(storeName)) {
                CachedKey cachedKey = propertyCache.getCache().get(key);
                if (key.startsWith(context) && cachedKey.getContentType().equals(KEY_VAULT_CONTENT_TYPE)) {
                    key = key.trim().substring(context.length()).replace('/', '.');
                    getKeyVaultValue(key, cachedKey.getValue(), cachedKey.getContentType(), clients);
                } else if (key.startsWith(context)) {
                    String trimedKey = key.trim().substring(context.length()).replace('/', '.');
                    properties.put(trimedKey, propertyCache.getCachedValue(key));
                } else {
                    List<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
                    ConfigurationSetting item = new ConfigurationSetting();
                    item.key(key);
                    item.value(propertyCache.getCachedValue(key));
                    item.contentType(FEATURE_FLAG_CONTENT_TYPE);
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
                ConfigurationSetting item = new ConfigurationSetting();
                item.key(key);
                item.value(propertyCache.getCachedValue(key));
                item.contentType(FEATURE_FLAG_CONTENT_TYPE);
                featureSet.addFeature(key.trim().substring(FEATURE_FLAG_PREFIX.length()), createFeature(
                        item));
            } catch (IOException e) {
                if (azureProperties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
        });
        if (featureSet.getFeatureManagement() != null) {
            properties.put(FEATURE_MANAGEMENT_KEY, mapper.convertValue(featureSet, LinkedHashMap.class));
        }
    }

    /**
     * Creates a {@code FeatureSet} from a list of {@code KeyValueItem}.
     * 
     * @param items New items read in from Azure
     * @param propertyCache Cached values where updated values are set.
     * @param date Cache timestamp
     * @throws IOException
     */
    private void createFeatureSet(List<ConfigurationSetting> settings, PropertyCache propertyCache,
            Date date)
            throws IOException {
        // Reading In Features
        FeatureSet featureSet = new FeatureSet();
        for (ConfigurationSetting item : settings) {
            Object feature = createFeature(item);
            if (feature != null) {
                featureSet.addFeature(item.key(), feature);
            }
        }

        if (featureSet != null && featureSet.getFeatureManagement() != null) {
            propertyCache.addKeyValuesToCache(settings, storeName, date);
        }
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     * 
     * @param item Used to create Features before being converted to be set into
     * properties.
     * @return Feature created from KeyValueItem
     * @throws IOException
     */
    private Object createFeature(ConfigurationSetting item) throws IOException {
        Feature feature = null;
        if (item.contentType().equals(FEATURE_FLAG_CONTENT_TYPE)) {
            try {
                FeatureManagementItem featureItem = mapper.readValue(item.value(), FeatureManagementItem.class);
                feature = new Feature(featureItem);

                // Setting Enabled For to null, but enabled = true will result in the
                // feature being on. This is the case of a feature is on/off and set to
                // on. This is to tell the difference between conditional/off which looks
                // exactly the same... It should never be the case of Conditional On, and
                // no filters coming from Azure, but it is a valid way from the config
                // file, which should result in false being returned.
                if (feature.getEnabledFor().size() == 0 && feature.getEnabled() == true) {
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
            LOGGER.error(String.format("Found Feature Flag %s with invalid Content Type of %s", item.key(),
                    item.contentType()));
            if (azureProperties.isFailFast()) {
                throw new IOException();
            }
        }
        return feature;
    }

    private void getKeyVaultValue(String key, String value, String contentType, ClientStore clients)
            throws IOException, URISyntaxException {
        if (contentType.equals(KEY_VAULT_CONTENT_TYPE)) {
            String uriString = value.substring(8, value.length() - 2);
            URI uri = new URI(uriString);

            KeyVaultClient keyVaultClient = clients.getKeyVaultClient(uri.getHost());
            if (keyVaultClient != null) {
                SecretBundle secretBundle = keyVaultClient.getSecret(uriString);
                properties.put(key, secretBundle.value());
            } else {
                logger.error("Found KeyVault Secret without an associated KeyVaultClient.");
                if (azureProperties.isFailFast()) {
                    throw new IOException(
                            "Failed Processing KeyVault Secret without an associated KeyVaultClient.");
                }
            }
        }
    }
}
