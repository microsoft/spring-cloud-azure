/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.net.URI;
import java.time.Duration;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.azure.spring.cloud.config.TokenCredentialProvider;

public class KeyVaultClient {

    private SecretAsyncClient secretClient;

    /**
     * Builds an Async client to a Key Vaults Secrets
     * 
     * @param uri Key Vault URI
     * @param tokenCredentialProvider user created credentials for authenticating to Key Vault
     */
    public KeyVaultClient(URI uri, TokenCredentialProvider tokenCredentialProvider) {
        TokenCredential tokenCredential = null;
        if (tokenCredentialProvider != null) {
            tokenCredential = tokenCredentialProvider.credentialForKeyVault();
        }
        if (tokenCredential == null) {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
        }
        secretClient = new SecretClientBuilder().vaultUrl("https://" + uri.getHost()).credential(tokenCredential)
                .buildAsyncClient();
    }

    /**
     * Gets the specified secret using the Secret Identifier
     * 
     * @param secretIdentifier The Secret Identifier to Secret
     * @param timeout How long it waits for a response from Key Vault
     * @return Secret values that matches the secretIdentifier
     */
    public KeyVaultSecret getSecret(URI secretIdentifier, int timeout) {
        String[] tokens = secretIdentifier.getPath().split("/");

        String name = (tokens.length >= 3 ? tokens[2] : null);
        String version = (tokens.length >= 4 ? tokens[3] : null);
        return secretClient.getSecret(name, version).block(Duration.ofSeconds(timeout));
    }

}
