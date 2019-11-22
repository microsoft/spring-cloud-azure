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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.TokenCredentialProvider;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;

public class ClientStore {

    private AppConfigProviderProperties appProperties;

    private ConnectionPool pool;

    private TokenCredentialProvider tokenCredentialProvider;

    public ClientStore(AppConfigProviderProperties appProperties,
            ConnectionPool pool, TokenCredentialProvider tokenCredentialProvider) {
        this.appProperties = appProperties;
        this.pool = pool;
        this.tokenCredentialProvider = tokenCredentialProvider;
    }

    private ConfigurationAsyncClient buildClient(String store) {
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        ExponentialBackoff retryPolicy = new ExponentialBackoff(appProperties.getMaxRetries(),
                Duration.ofMillis(800), Duration.ofSeconds(8));
        builder = builder.addPolicy(new BaseAppConfigurationPolicy()).retryPolicy(new RetryPolicy(
                retryPolicy));

        TokenCredential tokenCredential = null;
        Connection connection = pool.get(store);

        if (tokenCredentialProvider != null) {
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
     */

        return client.listConfigurationSettings(settingSelector).collectList().block();
    }

}
