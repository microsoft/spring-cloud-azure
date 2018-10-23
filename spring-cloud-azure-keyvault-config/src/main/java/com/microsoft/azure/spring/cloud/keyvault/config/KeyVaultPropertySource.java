/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretItem;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Retrieve all properties from the Azure Key Vault instance.
 */
public class KeyVaultPropertySource extends EnumerablePropertySource<KeyVaultClient> {

    private final KeyVaultClient keyVaultClient;
    private final String vaultUrl;

    // Saves names of all secrets in a Key Vault. Key is secret name. Value is null.
    private final ConcurrentHashMap<String, String> secretNames = new ConcurrentHashMap<>();
    // Saves values of each secret. Key is secret name. Value is secret value.
    private final ConcurrentHashMap<String, String> secretValues = new ConcurrentHashMap<>();

    public KeyVaultPropertySource(String name, KeyVaultClient keyVaultClient, String vaultUrl) {
        super(name, keyVaultClient);
        this.keyVaultClient = keyVaultClient;
        this.vaultUrl = vaultUrl;

        listSecretsFromKeyVault();
    }

    @Override
    public String[] getPropertyNames() {
        return this.secretNames.keySet().toArray(new String[this.secretValues.size()]);
    }

    @Override
    public Object getProperty(String name) {
        // NOTE:
        // Azure Key Vault secret name pattern is: ^[0-9a-zA-Z-]+$
        // "." is not allowed.
        final String normalizedName = name.replace(".", "-");

        if (this.secretNames.containsKey(normalizedName)) {
            return getSecretFromKeyVault(normalizedName);
        } else {
            return null;
        }
    }

    private void listSecretsFromKeyVault() {
        final PagedList<SecretItem> secrets = this.keyVaultClient.listSecrets(this.vaultUrl);
        secrets.loadAll();

        for (final SecretItem secret : secrets) {
            final String secretId = secret.id().replace(this.vaultUrl + "/secrets/", "");
            this.secretNames.putIfAbsent(secretId, "");
        }
    }

    private String getSecretFromKeyVault(final String secretName) {
        // Retrieve secret from Azure Key Vault service if not cached already.
        this.secretValues.computeIfAbsent(secretName, key -> this.keyVaultClient.getSecret(this.vaultUrl, key).value());
        return this.secretValues.get(secretName);
    }
}
