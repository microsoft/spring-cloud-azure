/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class ClientStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStore.class);

    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";

    private HashMap<String, ConfigurationAsyncClient> configClients;

    private AppConfigProviderProperties appProperties;

    public ClientStore(AzureCloudConfigProperties properties, AppConfigProviderProperties appProperties,
            ConnectionStringPool pool) {
        this.appProperties = appProperties;
        
        configClients = new HashMap<String, ConfigurationAsyncClient>();
        for (String store : pool.getAll().keySet()) {
            try {
                ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
                builder = builder.addPolicy(new BaseAppConfigurationPolicy());

                // Using Connection String not Managed Identity
                if (StringUtils.isNotEmpty(pool.get(store).getFullConnectionString())) {
                    builder.connectionString(pool.get(store).getFullConnectionString());
                } else {
                    throw new IllegalArgumentException("Connections String can't be empty or null");
                }

                ConfigurationAsyncClient client = builder.buildAsyncClient();

                configClients.put(store, client);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to load Config Store.");
                if (properties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(new Exception("Failed to load Config Store.", e));
                }
            }
        }
    }

    public ConfigurationAsyncClient getConfigurationClient(String storeName) {
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
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        List<ConfigurationSetting> configSettings = null;
        boolean retry = true;
        int retryCount = 0;

        while (configSettings == null && retry) {
            try {
                configSettings = fluxResponseToListTest(client.listSettingRevisions(settingSelector));
            } catch (IllegalArgumentException e) {
                retry = false;
            } catch (HttpResponseException e) {
                retry = retryIfFailed(e.getResponse(), retryCount);
                retryCount++;
            }
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
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        List<ConfigurationSetting> configSettings = null;
        boolean retry = true;
        int retryCount = 0;

        while (configSettings == null && retry) {
            try {
                configSettings = fluxResponseToListTest(client.listSettings(settingSelector));
            } catch (IllegalArgumentException e) {
                retry = false;
            } catch (HttpResponseException e) {
                retry = retryIfFailed(e.getResponse(), retryCount);
                retryCount++;
            }
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
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        ConfigurationSetting configSetting = null;
        boolean retry = true;
        int retryCount = 0;

        while (configSetting == null && retry) {
            try {
                Mono<ConfigurationSetting> settingMono = client.getSetting(setting, storeName);
                configSetting = settingMono.block();
            } catch (IllegalArgumentException e) {
                retry = false;
            } catch (HttpResponseException e) {
                retry = retryIfFailed(e.getResponse(), retryCount);
                retryCount++;

                if (!retry) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
        }
        return configSetting;
    }

    
    /**
     * Takes a PagedFlux of Configuration Settings and converts it to a List of Configuration Settings.
     * 
     * @param response PagedFlux respose to be converted to a list of Configuration Settings.
     * @return A List of configuration Settings.
     */
    private final List<ConfigurationSetting> fluxResponseToListTest(PagedFlux<ConfigurationSetting> response) {
        List<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
        Disposable ready = response.byPage().subscribe(page -> items.addAll(page.getItems()));
        while (!ready.isDisposed()) {
        }
        return items;
    }

    /**
     * Checks based of the response an the amount of retries already to see if another attempt should be made.
     * 
     * @param response The server response
     * @param retryCount The number of retries that have already been made
     * @return True if a retry attempt should be made.
     * @throws ServerException An invalid retry-after-ms header has returned.
     */
    private boolean retryIfFailed(HttpResponse response, int retryCount) throws ServerException {
        HttpHeader retryHeader = response.getHeaders().get(RETRY_AFTER_MS_HEADER);
        long retryLength = 0;
        if (retryHeader != null) {
            String retryValue = retryHeader.getValue();
            if (NumberUtils.isCreatable(retryValue)) {
                try {
                    retryLength = Long.valueOf(retryValue);
                } catch (NumberFormatException nfe) {
                    throw new ServerException("Server Returned Invalid retry-after-ms header.", nfe);
                }

            }
        } else {
            return false;
        }

        Date startDate = appProperties.getStartDate();
        Date maxRetryDate = DateUtils.addSeconds(startDate, appProperties.getMaxRetryTime());

        if (retryCount < appProperties.getMaxRetries() && !startDate.after(maxRetryDate)) {
            try {
                // Need to wait before Retry, need to figure out how long.
                long retryBackoff = (new Double(Math.pow(2, retryCount))).longValue() - 1;
                // Adds Jitter to unsync possible concurrent retry attempts
                long jitter = 0;
                if (retryBackoff != 0) {
                    jitter = retryLength * ThreadLocalRandom.current().nextLong(0, retryBackoff);
                }
                // Minimum sleep time retry is the Server returned value
                Thread.sleep(Math.max(retryLength, jitter));
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait before retry.", e);
            }
        } else {
            return false;
        }
        return true;
    }

    public HashMap<String, ConfigurationAsyncClient> getConfigurationClient() {
        return configClients;
    }

}
