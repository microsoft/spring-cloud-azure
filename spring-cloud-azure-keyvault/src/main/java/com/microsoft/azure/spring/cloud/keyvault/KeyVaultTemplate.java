/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault;

import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.spring.cloud.context.core.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.Tuple;
import com.microsoft.rest.RestClient;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link KeyVaultOperation}
 *
 * @author Warren Zhu
 */
public class KeyVaultTemplate implements KeyVaultOperation {
    private static final String KEY_VAULT_URL = "https://%s.vault.azure.net/";
    private final Map<String, Set<String>> secretNamesByKeyVault = new ConcurrentHashMap<>();
    private final Map<Tuple<String, String>, String> secretsByKeyVault = new ConcurrentHashMap<>();

    private final AzureKeyVaultCredential credential;

    private Function<String, KeyVaultClient> keyVaultClientCreator = keyVaultClientCreator();

    public KeyVaultTemplate(String clientId, String clientSecret) {
        this.credential = new AzureKeyVaultCredential(clientId, clientSecret);
    }

    private static String buildKeyVaultUrl(String keyVaultName) {
        return String.format(KEY_VAULT_URL, keyVaultName);
    }

    @Override
    public String getSecret(String keyVaultName, String secretName, boolean fromCache) {
        Tuple<String, String> keyVaultAndSecret = Tuple.of(keyVaultName, secretName);
        if (fromCache) {
            this.secretsByKeyVault.computeIfAbsent(keyVaultAndSecret, this::fetchSecret);
        } else {
            this.secretsByKeyVault.put(keyVaultAndSecret, fetchSecret(keyVaultAndSecret));
        }

        return this.secretsByKeyVault.get(keyVaultAndSecret);
    }

    @Override
    public Collection<String> listSecrets(String keyVaultName, boolean fromCache) {
        if (fromCache) {
            this.secretNamesByKeyVault.computeIfAbsent(keyVaultName, this::fetchSecretNames);
        } else {
            this.secretNamesByKeyVault.put(keyVaultName, fetchSecretNames(keyVaultName));
        }

        return this.secretNamesByKeyVault.get(keyVaultName);
    }

    private Set<String> fetchSecretNames(String keyVaultName) {
        String keyVaultUrl = buildKeyVaultUrl(keyVaultName);
        PagedList<SecretItem> secretItems = this.keyVaultClientCreator.apply(keyVaultName).listSecrets(keyVaultUrl);
        return Collections.unmodifiableSet(
                secretItems.stream().map(SecretItem::id).map(s -> s.substring(s.lastIndexOf('/') + 1))
                           .collect(Collectors.toSet()));
    }

    private String fetchSecret(Tuple<String, String> keyVaultAndSecret) {
        String keyVaultName = keyVaultAndSecret.getFirst();
        return this.keyVaultClientCreator.apply(keyVaultName)
                                         .getSecret(buildKeyVaultUrl(keyVaultName), keyVaultAndSecret.getSecond())
                                         .value();
    }

    private KeyVaultClient buildKeyVaultClient(String keyVaultName) {
        String keyVaultUrl = buildKeyVaultUrl(keyVaultName);
        RestClient restClient = new RestClient.Builder().withBaseUrl(keyVaultUrl).withCredentials(this.credential)
                                                        .withSerializerAdapter(new AzureJacksonAdapter())
                                                        .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                                                        .withUserAgent("").build();
        return new KeyVaultClient(restClient);
    }

    private Function<String, KeyVaultClient> keyVaultClientCreator() {
        return Memoizer.memoize(this::buildKeyVaultClient);
    }

}
