/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.azure.spring.cloud.config.AppConfigCredentialProvider;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;

public class ClientStore {

    private AppConfigProviderProperties appProperties;

    private ConnectionPool pool;

    private AppConfigCredentialProvider tokenCredentialProvider;

    public ClientStore(AppConfigProviderProperties appProperties,
            ConnectionPool pool, AppConfigCredentialProvider tokenCredentialProvider) {
        this.appProperties = appProperties;
        this.pool = pool;
        this.tokenCredentialProvider = tokenCredentialProvider;
    }

    private ConfigurationAsyncClient buildClient(String store) throws IllegalArgumentException {
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        ExponentialBackoff retryPolicy = new ExponentialBackoff(appProperties.getMaxRetries(),
                Duration.ofMillis(800), Duration.ofSeconds(8));
        builder = builder.addPolicy(new BaseAppConfigurationPolicy()).retryPolicy(new RetryPolicy(
                retryPolicy));

        TokenCredential tokenCredential = null;
        Connection connection = pool.get(store);

        String endpoint = connection.getEndpoint();

        if (tokenCredentialProvider != null) {
            tokenCredential = tokenCredentialProvider.getAppConfigCredential(endpoint);
        }
        if ((tokenCredential != null
                || (connection.getClientId() != null && StringUtils.isNotEmpty(connection.getClientId())))
                && (connection != null && StringUtils.isNotEmpty(connection.getConnectionString()))) {
            throw new IllegalArgumentException(
                    "More than 1 Conncetion method was set for connecting to App Configuration.");
        } else if (tokenCredential != null && connection != null && connection.getClientId() != null
                && StringUtils.isNotEmpty(connection.getClientId())) {
            throw new IllegalArgumentException(
                    "More than 1 Conncetion method was set for connecting to App Configuration.");
        }
        
        if (tokenCredential != null) {
            // User Provided Token Credential
            builder.credential(tokenCredential);
        } else if ((connection.getClientId() != null && StringUtils.isNotEmpty(connection.getClientId()))
                && connection.getEndpoint() != null) {
            // User Assigned Identity - Client ID through configuration file.
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder()
                    .clientId(connection.getClientId());
            builder.credential(micBuilder.build());
        } else if (StringUtils.isNotEmpty(connection.getConnectionString())) {
            // Connection String
            builder.connectionString(connection.getConnectionString());
        } else if (connection.getEndpoint() != null) {
            // System Assigned Identity. Needs to be checked last as all of the above should have a Endpoint.
            ManagedIdentityCredentialBuilder micBuilder = new ManagedIdentityCredentialBuilder();
            builder.credential(micBuilder.build());
        } else {
            throw new IllegalArgumentException("No Configuration method was set for connecting to App Configuration");
        }
        return builder.endpoint(endpoint).buildAsyncClient();
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
        ConfigurationAsyncClient client = buildClient(storeName);

        return client.listRevisions(settingSelector).collectList().block();
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
        ConfigurationAsyncClient client = buildClient(storeName);

        return client.listConfigurationSettings(settingSelector).collectList().block();
    }

}
