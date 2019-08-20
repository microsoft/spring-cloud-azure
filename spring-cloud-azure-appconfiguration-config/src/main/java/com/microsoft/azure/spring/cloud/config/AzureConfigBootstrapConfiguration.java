/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;
import com.microsoft.azure.spring.cloud.config.stores.KeyVaultStore;
import com.microsoft.azure.spring.cloud.context.core.config.AzureManagedIdentityProperties;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestClient.Builder;
import com.microsoft.rest.protocol.ResponseBuilder.Factory;

@Configuration
@EnableConfigurationProperties(AzureCloudConfigProperties.class)
@ConditionalOnClass(AzureConfigPropertySourceLocator.class)
@ConditionalOnProperty(prefix = AzureCloudConfigProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
public class AzureConfigBootstrapConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigBootstrapConfiguration.class);

    private static final String ENV_MSI_ENDPOINT = "MSI_ENDPOINT";

    private static final String ENV_MSI_SECRET = "MSI_SECRET";

    private static final String TELEMETRY_SERVICE = "AppConfiguration";

    private static final String TELEMETRY_KEY = "HashedStoreName";

    @Bean
    public AzureTokenCredentials tokenCredentials(AzureCloudConfigProperties properties) {
        if (StringUtils.hasText(System.getenv(ENV_MSI_ENDPOINT))
                && StringUtils.hasText(System.getenv(ENV_MSI_SECRET))) {
            return new AppServiceMSICredentials(AzureEnvironment.AZURE);
        }

        AzureManagedIdentityProperties msiProps = properties.getManagedIdentity();
        MSICredentials credentials = new MSICredentials();
        if (msiProps != null && msiProps.getClientId() != null) {
            credentials.withClientId(msiProps.getClientId());
        } else if (msiProps != null && msiProps.getObjectId() != null) {
            credentials.withObjectId(msiProps.getObjectId());
        }

        return credentials;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public AzureConfigPropertySourceLocator sourceLocator(
            AzureCloudConfigProperties properties, PropertyCache propertyCache,
            HashMap<String, KeyVaultClient> keyVaultClients, HashMap<String, ConfigurationAsyncClient> configClients) {
        return new AzureConfigPropertySourceLocator(properties, propertyCache, keyVaultClients,
                configClients);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyCache getPropertyCache() {
        return new PropertyCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public HashMap<String, KeyVaultClient> getKeyVaultClients(AzureCloudConfigProperties properties) {
        HashMap<String, KeyVaultClient> keyVaultClients = new HashMap<String, KeyVaultClient>();
        AzureEnvironment environment = properties.getEnvironment();
        for (KeyVaultStore keyVaultStore : properties.getKeyVaultStores()) {
            String clientId = keyVaultStore.getClientId();
            String domain = keyVaultStore.getDomain();
            String secret = keyVaultStore.getSecret();
            String baseUrl = keyVaultStore.getConnectionUrl();

            AzureJacksonAdapter azureJacksonAdapter = new AzureJacksonAdapter();
            Factory factory = new AzureResponseBuilder.Factory();

            AzureTokenCredentials credentials = new ApplicationTokenCredentials(clientId, domain, secret, environment);

            RestClient restClient = new Builder().withBaseUrl(baseUrl).withCredentials(credentials)
                    .withSerializerAdapter(azureJacksonAdapter).withResponseBuilderFactory(factory).build();

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
        return keyVaultClients;
    }

    @Bean
    public HashMap<String, ConfigurationAsyncClient> getConfigurationClient(AzureCloudConfigProperties properties) {
        HashMap<String, ConfigurationAsyncClient> configClients = new HashMap<String, ConfigurationAsyncClient>();
        for (ConfigStore store : properties.getStores()) {
            try {
                ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                        .credential(new ConfigurationClientCredentials(store.getConnectionString())).buildAsyncClient();
                configClients.put(store.getName(), client);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                LOGGER.error("Failed to load Config Store.");
                if (properties.isFailFast()) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
        }
        return configClients;
    }

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(TELEMETRY_SERVICE);
    }
}
