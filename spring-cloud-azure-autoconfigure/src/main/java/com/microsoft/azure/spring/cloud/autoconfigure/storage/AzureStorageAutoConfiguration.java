/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureProperties;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(CloudStorageAccount.class)
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageAutoConfiguration {
    private static final Log LOGGER = LogFactory.getLog(AzureStorageAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public CloudStorageAccount storage(Azure.Authenticated authenticated, AzureProperties azureProperties,
                                       AzureStorageProperties azureStorageProperties) throws IOException {
        String accountName = azureStorageProperties.getAccount();

        StorageAccount storageAccount = authenticated.withDefaultSubscription().storageAccounts()
                .getByResourceGroup(azureProperties.getResourceGroup(), accountName);
        Optional<StorageAccountKey> key = storageAccount.getKeys().stream().findAny();

        String connectionString = ConnectionStringBuilder.build(accountName, key.get().value());
        if (key.isPresent()) {
            try {
                return CloudStorageAccount.parse(connectionString);
            } catch (URISyntaxException | InvalidKeyException e) {
                LOGGER.error("Failed to parse connection string" + connectionString, e);
            }
        }

        throw new RuntimeException("Storage account key is empty.");
    }

    static class ConnectionStringBuilder {
        private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

        private static final String ACCOUNT_NAME = "AccountName";

        private static final String ACCOUNT_KEY = "AccountKey";

        private static final String ENDPOINT_SUFFIX = "EndpointSuffix";

        private static final String HTTP_PROTOCOL = "http";

        private static final String DEFAULT_ENDPOINT_SUFFIX = "core.windows.net";

        private static final String SEPARATOR = ";";

        static String build(String accountName, String accountKey) {
            Map<String, String> map = new HashMap<>();
            map.put(DEFAULT_PROTOCOL, HTTP_PROTOCOL);
            map.put(ACCOUNT_NAME, accountName);
            map.put(ACCOUNT_KEY, accountKey);
            map.put(ENDPOINT_SUFFIX, DEFAULT_ENDPOINT_SUFFIX);

            return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
        }
    }
}
