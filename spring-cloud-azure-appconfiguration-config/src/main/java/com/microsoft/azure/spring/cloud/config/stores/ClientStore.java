/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.rmi.ServerException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;

public class ClientStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStore.class);

    private HashMap<String, ConfigurationClient> configClients;

    public ClientStore(AzureCloudConfigProperties properties, AppConfigProviderProperties appProperties,
            ConnectionStringPool pool) {

        configClients = new HashMap<String, ConfigurationClient>();
        for (String store : pool.getAll().keySet()) {
            try {
                ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
                builder = builder.addPolicy(new BaseAppConfigurationPolicy()).retryPolicy(new RetryPolicy(
                        appProperties.getMaxRetries(), Duration.ofSeconds(appProperties.getMaxRetryTime())));

                // Using Connection String not Managed Identity
                if (StringUtils.isNotEmpty(pool.get(store).getFullConnectionString())) {
                    builder.connectionString(pool.get(store).getFullConnectionString());
                } else {
                    throw new IllegalArgumentException("Connections String can't be empty or null");
                }

                ConfigurationClient client = builder.buildClient();

                configClients.put(store, client);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to load Config Store.");
                if (properties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(new Exception("Failed to load Config Store.", e));
                }
            }
        }
    }

    public ConfigurationClient getConfigurationClient(String storeName) {
        return configClients.get(storeName);
    }

    /**
     * Gets a list of Configuration Settings from the revisions given config store that
     * match the Setting Selector criteria.
     * 
     * @param settingSelector Information on which setting to pull. i.e. number of
     * results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     * @throws ServerException thrown when retry-after-ms has invalid value.
     */
    public final List<ConfigurationSetting> listSettingRevisons(SettingSelector settingSelector, String storeName)
            throws ServerException {
        ConfigurationClient client = getConfigurationClient(storeName);
        List<ConfigurationSetting> configSettings = null;

        while (configSettings == null) {
            configSettings = pagedIterableToList(client.listSettingRevisions(settingSelector));
        }
        return configSettings;
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the
     * Setting Selector criteria.
     * 
     * @param settingSelector Information on which setting to pull. i.e. number of
     * results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     * @throws ServerException thrown when retry-after-ms has invalid value.
     */
    public final List<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName)
            throws ServerException {
        ConfigurationClient client = getConfigurationClient(storeName);
        List<ConfigurationSetting> configSettings = null;

        while (configSettings == null) {
            configSettings = pagedIterableToList(client.listSettings(settingSelector));
        }
        return configSettings;
    }

    /**
     * Gets a single Configuration Setting from the given config store that match the
     * setting key.
     * 
     * @param setting Name of the key in the config store.
     * @param storeName Name of the App Configuration store to query against.
     * @return A Configuration Setting.
     * @throws ServerException thrown when retry-after-ms has invalid value.
     */
    public final ConfigurationSetting getSetting(String setting, String storeName) throws ServerException {
        ConfigurationClient client = getConfigurationClient(storeName);
        return client.getSetting(setting, storeName);
    }

    /**
     * Takes a PagedFlux of Configuration Settings and converts it to a List of
     * Configuration Settings.
     * 
     * @param response PagedFlux respose to be converted to a list of Configuration
     * Settings.
     * @return A List of configuration Settings.
     */
    private final List<ConfigurationSetting> pagedIterableToList(PagedIterable<ConfigurationSetting> response) {
        List<ConfigurationSetting> settings = new ArrayList<ConfigurationSetting>();
        for (ConfigurationSetting setting : response) {
            settings.add(setting);
        }
        return settings;
    }

    public HashMap<String, ConfigurationClient> getConfigurationClient() {
        return configClients;
    }

}
