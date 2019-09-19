/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestClient.Builder;
import com.microsoft.rest.protocol.ResponseBuilder.Factory;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class ClientStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStore.class);

    private HashMap<String, ConfigurationAsyncClient> configClients;

    private HashMap<String, KeyVaultClient> keyVaultClients;

    private AppConfigProviderProperties appProperties;

    public ClientStore(AzureCloudConfigProperties properties, AppConfigProviderProperties appProperties) {
        this.appProperties = appProperties;
        buildConfigurationClientStores(properties);
        buildKeyVaultClients(properties);
    }

    private void buildConfigurationClientStores(AzureCloudConfigProperties properties) {
        configClients = new HashMap<String, ConfigurationAsyncClient>();
        for (ConfigStore store : properties.getStores()) {
            try {
                ConfigurationClientBuilder builder = new ConfigurationClientBuilder()
                        .addPolicy(new BaseAppConfigurationPolicy());

                // Using Connection String not Managed Identity
                if (StringUtils.isNotEmpty(store.getConnectionString())) {
                    builder.credential(new ConfigurationClientCredentials(store.getConnectionString()));
                } else {
                    throw new IllegalArgumentException("Connections String can't be empty or null");
                }

                ConfigurationAsyncClient client = builder.buildAsyncClient();

                configClients.put(store.getName(), client);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                LOGGER.error("Failed to load Config Store.");
                if (properties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(new Exception("Failed to load Config Store.", e));
                }
            }
        }
    }

    private void buildKeyVaultClients(AzureCloudConfigProperties properties) {
        keyVaultClients = new HashMap<String, KeyVaultClient>();
        AzureEnvironment environment = properties.getEnvironment();
        for (KeyVaultStore keyVaultStore : properties.getKeyVaultStores()) {
            String clientId = keyVaultStore.getClientId();
            String domain = keyVaultStore.getDomain();
            String secret = keyVaultStore.getSecret();
            String baseUrl = keyVaultStore.getConnectionUrl();

            AzureJacksonAdapter azureJacksonAdapter = new AzureJacksonAdapter();
            Factory factory = new AzureResponseBuilder.Factory();

            AzureTokenCredentials credentials = new ApplicationTokenCredentials(clientId, domain, secret, environment);

            Builder builder = new Builder();
            builder.withBaseUrl(baseUrl);
            builder.withCredentials(credentials);
            builder.withSerializerAdapter(azureJacksonAdapter);
            builder.withResponseBuilderFactory(factory);

            RestClient restClient = builder.build();

            try {
                URI uri = new URI(baseUrl);

                keyVaultClients.put(uri.getHost(), new KeyVaultClient(restClient));
            } catch (URISyntaxException e) {
                LOGGER.error("Failed to parse KeyVaultHost.");
                if (properties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
        }
    }

    public ConfigurationAsyncClient getConfigurationClient(String storeName) {
        return configClients.get(storeName);
    }

    public KeyVaultClient getKeyVaultClient(String storeName) {
        return keyVaultClients.get(storeName);
    }

    /**
     * 
     * @param settingSelector
     * @param storeName
     * @return
     */
    public final List<ConfigurationSetting> listSettingRevisons(SettingSelector settingSelector, String storeName) {
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        return fluxResponseToListTest(client.listSettingRevisions(settingSelector));
    }

    /**
     * 
     * @param settingSelector
     * @param storeName
     * @return
     */
    public final List<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName) {
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        return fluxResponseToListTest(client.listSettings(settingSelector));
    }

    /**
     * 
     * @param settingSelector
     * @param storeName
     * @return
     */
    public final ConfigurationSetting getSetting(String setting, String storeName) {
        ConfigurationAsyncClient client = getConfigurationClient(storeName);
        try {
            Mono<ConfigurationSetting> settingMono = client.getSetting(setting);
            return settingMono.block();
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            
        } catch (HttpResponseException e) {
            
        }
        return null;
    }

    /**
     * 
     * @param response
     * @return
     */
    private final List<ConfigurationSetting> fluxResponseToListTest(PagedFlux<ConfigurationSetting> response) {
        List<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
        Disposable ready = response.byPage().subscribe(page -> items.addAll(page.items()));
        while (!ready.isDisposed()) {
        }
        return items;
    }

    public HashMap<String, KeyVaultClient> getKeyVaultClients() {
        return keyVaultClients;
    }

    public HashMap<String, ConfigurationAsyncClient> getConfigurationClient() {
        return configClients;
    }

    private int retryIfFailed(Date startDate, int retryCount, long retryLength) {
        Date maxRetryDate = DateUtils.addSeconds(startDate, appProperties.getMaxRetryTime());

        while (retryCount < appProperties.getMaxRetries() && !startDate.after(maxRetryDate)) {
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
        }
        return retryCount + 1;
    }

}
