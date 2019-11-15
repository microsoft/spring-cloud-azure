/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.azure.spring.cloud.config.TokenCredentialProvider;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;

import reactor.core.Disposable;

public class ClientStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStore.class);

    private HashMap<String, ConfigurationAsyncClient> configClients;

    private AppConfigProviderProperties appProperties;

    private AzureCloudConfigProperties properties;

    private ConnectionPool pool;

    public ClientStore(AzureCloudConfigProperties properties, AppConfigProviderProperties appProperties,
            ConnectionPool pool) {
        this.appProperties = appProperties;
        this.properties = properties;
        this.configClients = new HashMap<String, ConfigurationAsyncClient>();
        this.pool = pool;
    }

    public void build(TokenCredentialProvider tokenCredentialProvider) {
        for (String store : pool.getAll().keySet()) {
            try {
                ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
                ExponentialBackoff retryPolicy = new ExponentialBackoff(appProperties.getMaxRetries(),
                        Duration.ofMillis(800), Duration.ofSeconds(8));
                builder = builder.addPolicy(new BaseAppConfigurationPolicy()).retryPolicy(new RetryPolicy(
                        retryPolicy));

                TokenCredential tokenCredential = null;
                Connection connection = pool.get(store);
                if (tokenCredentialProvider != null) {
                    LOGGER.error("Using User Created App Config Credential.");
                    tokenCredential = tokenCredentialProvider.credentialForAppConfig();
                }
                if (tokenCredential != null) {
                    builder.credential(tokenCredential);
                } else if (connection.getClientId() != null) {
                    ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder()
                            .clientId(connection.getClientId());
                    builder.credential(micBuilder.build());
                } else if (StringUtils.isNotEmpty(connection.getConnectionString())) {
                    builder.connectionString(connection.getConnectionString());
                } else {
                    builder.credential(new DefaultAzureCredentialBuilder().build());
                }
                String endpoint = "https://" + store + ".azconfig.io";
                builder.endpoint(endpoint);

                ConfigurationAsyncClient client = builder.buildAsyncClient();

                configClients.put(store, client);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to load Config Store.");
                if (properties.isFailFast()) {
                    throw new RuntimeException("Failed to load Config Store.", e);
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
     */
    public final List<ConfigurationSetting> listSettingRevisons(SettingSelector settingSelector, String storeName) {
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        return client.listRevisions(settingSelector).byPage().next()
                .block(Duration.ofSeconds(appProperties.getMaxRetryTime())).getItems();
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the
     * Setting Selector criteria.
     * 
     * @param settingSelector Information on which setting to pull. i.e. number of
     * results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     * @throws IOException thrown when failed to retrieve values.
     */
    public final List<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName)
            throws IOException {
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        return fluxResponseToList(client.listConfigurationSettings(settingSelector));
    }

    /**
     * Takes a PagedFlux of Configuration Settings and converts it to a List of
     * Configuration Settings.
     * 
     * @param response PagedFlux respose to be converted to a list of Configuration
     * Settings.
     * @return A List of configuration Settings or Null on error.
     */
    private final List<ConfigurationSetting> fluxResponseToList(PagedFlux<ConfigurationSetting> response) {
        ExecutorService executor = Executors.newCachedThreadPool();

        Callable<List<ConfigurationSetting>> callable = new Callable<List<ConfigurationSetting>>() {
            @Override
            public List<ConfigurationSetting> call() throws Exception {
                try {
                    List<ConfigurationSetting> settings = new ArrayList<ConfigurationSetting>();
                    Disposable ready = response.byPage().subscribe(page -> settings.addAll(page.getItems()));
                    while (!ready.isDisposed()) {
                    }
                    return settings;
                } catch (Exception e) {
                    LOGGER.error("Request error.", e);
                    throw e;
                }
            }
        };
        Future<List<ConfigurationSetting>> configurationSettingFuture = executor.submit(callable);

        try {
            return configurationSettingFuture.get(appProperties.getMaxRetryTime(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Error thrown when retreiving configurations.", e);
        }
        return null;
    }

    HashMap<String, ConfigurationAsyncClient> getConfigurationClient() {
        return configClients;
    }

}
