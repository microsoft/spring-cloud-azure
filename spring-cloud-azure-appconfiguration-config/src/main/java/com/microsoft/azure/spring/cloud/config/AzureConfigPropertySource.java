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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private AppConfigProviderProperties appProperties;

    private static ObjectMapper mapper = new ObjectMapper();

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    private HashMap<String, SecretAsyncClient> keyVaultClients;

    private ClientStore clients;

    public AzureConfigPropertySource(String context, String storeName,
            String label, AzureCloudConfigProperties azureProperties, AppConfigProviderProperties appProperties,
            ClientStore clients) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely
        // define a PropertySource
        super(context + storeName + "/" + label);
        this.context = context;
        this.storeName = storeName;
        this.label = label;
        this.azureProperties = azureProperties;
        this.appProperties = appProperties;
        this.keyVaultClients = new HashMap<String, SecretAsyncClient>();
        this.clients = clients;
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
     * <p>
     * Gets settings from Azure/Cache to set as configurations. Updates the cache.
     * </p>
     * 
     * <p>
     * <b>Note</b>: Doesn't update Feature Management, just stores values in cache. Call
     * {@code initFeatures} to update Feature Management, but make sure its done in the
     * last {@code AzureConfigPropertySource}
     * </p>
     * 
     * @param propertyCache Cached values to use in store. Also contains values the need
     * to be refreshed.
     * @throws IOException Thrown when processing key/value failed when reading feature
     * flags
     */
    public void initProperties(PropertyCache propertyCache) throws IOException {
        Date date = new Date();
        SettingSelector settingSelector = new SettingSelector();
        if (!label.equals("%00")) {
            settingSelector.labels(label);
        }

        if (propertyCache.getContext(storeName) == null) {
            propertyCache.addContext(storeName, context);
            // * for wildcard match
            settingSelector.keys(context + "*");
            List<ConfigurationSetting> settings = clients.listSettings(settingSelector, storeName);
            for (ConfigurationSetting setting : settings) {
                String key = setting.key().trim().substring(context.length()).replace('/', '.');
                if (setting.contentType().equals(KEY_VAULT_CONTENT_TYPE)) {
                    String entry = getKeyVaultEntry(setting.value());

                    // Null in the case of failFast is false, will just skip entry.
                    if (entry != null) {
                        properties.put(key, entry);
                    }
                } else {
                    properties.put(key, setting.value());
                }

            }
            propertyCache.addKeyValuesToCache(settings, storeName, date);

            // Reading In Features
            settingSelector.keys("*appconfig*");
            settings = clients.listSettings(settingSelector, storeName);

            createFeatureSet(settings, propertyCache, date);

        } else {
            // Using Cached values, first updates cache then sets new properties.
            if (propertyCache.getRefreshKeys(storeName) != null &&
                    propertyCache.getRefreshKeys(storeName).size() > 0) {
                for (String refreshKey : propertyCache.getRefreshKeys(storeName)) {
                    settingSelector.keys(refreshKey);
                    ConfigurationSetting setting = clients.getSetting(refreshKey, storeName);
                    if (setting == null) {
                        ConfigurationSetting emptyKey = new ConfigurationSetting();
                        emptyKey.key(refreshKey);
                        propertyCache.addToCache(emptyKey, storeName, date);
                    } else {
                        propertyCache.addToCache(setting, storeName, date);
                    }
                }
            }

            List<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
            Set<CachedKey> cachedKeys = propertyCache.getKeySet(storeName);
            for (CachedKey cachedKey : cachedKeys) {
                if (cachedKey.contentType() == null) {
                    String trimedKey = cachedKey.key().trim().substring(context.length()).replace('/', '.');
                    properties.put(trimedKey, propertyCache.getCachedValue(cachedKey.key()));
                    propertyCache.removeFromCache(cachedKey);
                } else if (cachedKey.contentType().equals(KEY_VAULT_CONTENT_TYPE)) {
                    String key = cachedKey.key().trim().substring(context.length()).replace('/', '.');
                    String entry = getKeyVaultEntry(cachedKey.value());

                    // Null in the case of failFast is false, will just skip entry.
                    if (entry != null) {
                        properties.put(key, entry);
                    }
                } else if (cachedKey.contentType().equals(FEATURE_FLAG_CONTENT_TYPE)) {
                    items.add(cachedKey.getSetting());
                } else {
                    String trimedKey = cachedKey.key().trim().substring(context.length()).replace('/', '.');
                    properties.put(trimedKey, propertyCache.getCachedValue(cachedKey.key()));
                }
            }
            createFeatureSet(items, propertyCache, date);
        }
    }

    /**
     * Given a Setting's Key Vault Reference stored in the Settings value, it will get its
     * entry in Key Vault.
     * 
     * @param value {"uri":
     * "&lt;your-vault-url&gt;/secret/&lt;secret&gt;/&lt;version&gt;"}
     * @return Key Vault Secret Value
     */
    private String getKeyVaultEntry(String value) {
        String secretValue = null;
        try {
            String stringUri = "";
            URI uri = null;
            Secret secret = new Secret();

            // Parsing Key Vault Reference for URI
            try {
                JsonNode kvReference = mapper.readTree(value);
                stringUri = kvReference.at("/uri").asText();
                uri = new URI(stringUri);
            } catch (URISyntaxException e) {
                if (azureProperties.isFailFast()) {
                    LOGGER.error("Error Processing Key Vault Entry URI.", e);
                } else {
                    LOGGER.error("Error Processing Key Vault Entry URI.");
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }

            // If no entry found don't connect to Key Vault
            if (stringUri.equals("") || uri == null) {
                if (azureProperties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(
                            new IOException("Invaid URI when parsing Key Vault Reference."));
                } else {
                    return null;
                }
            }

            // Setting the Key Vault Reference as ID enables search by id and version
            secret.id(stringUri);

            // Check if we already have a client for this key vault, if not we will make
            // one
            if (!keyVaultClients.containsKey(uri.getHost())) {
                SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
                        .endpoint("https://" + uri.getHost())
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildAsyncClient();
                keyVaultClients.put(uri.getHost(), secretAsyncClient);
            }
            Duration keyVaultWaitTime = Duration.ofSeconds(appProperties.getKeyVaultWaitTime());
            secret = keyVaultClients.get(uri.getHost()).getSecret(secret).block(keyVaultWaitTime);
            if (secret == null) {
                throw new IOException("No Key Vault Secret found for Reference.");
            }
            secretValue = secret.value();
        } catch (RuntimeException | IOException e) {
            if (azureProperties.isFailFast()) {
                LOGGER.error("Error Retreiving Key Vault Entry", e);
            } else {
                LOGGER.error("Error Retreiving Key Vault Entry");
                ReflectionUtils.rethrowRuntimeException(e);
            }
        }
        return secretValue;
    }

    /**
     * Initializes Feature Management configurations. Only one
     * {@code AzureConfigPropertySource} can call this, and it needs to be done after the
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
                featureSet.addFeature(key.trim().substring(FEATURE_FLAG_PREFIX.length()),
                        createFeature(propertyCache.getCache().get(key)));
            } catch (Exception e) {
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
    private void createFeatureSet(List<ConfigurationSetting> settings, PropertyCache propertyCache, Date date)
            throws IOException {
        // Reading In Features
        FeatureSet featureSet = new FeatureSet();
        for (ConfigurationSetting setting : settings) {
            Object feature = createFeature(setting);
            if (feature != null) {
                String key = setting.key().trim().substring(FEATURE_FLAG_PREFIX.length());
                featureSet.addFeature(key, feature);
            }
        }
        if (featureSet.getFeatureManagement() != null) {
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
            String message = String.format("Found Feature Flag %s with invalid Content Type of %s", item.key(),
                    item.contentType());

            if (azureProperties.isFailFast()) {
                throw new IOException(message);
            }
            LOGGER.error(message);
        }
        return feature;
    }
}
