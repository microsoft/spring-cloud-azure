/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import java.net.URI;
import java.time.Duration;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.Secret;

public class KeyVaultClient {

    private SecretAsyncClient secretAsyncClient;

    /**
     * Builds an Async client to a Key Vaults Secrets
     * 
     * @param uri Key Vault URI
     */
    public KeyVaultClient(URI uri) {
        secretAsyncClient = new SecretClientBuilder()
                .endpoint("https://" + uri.getHost())
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
    }

    /**
     * Gets the specified secret using the Secret Identifier
     * 
     * @param secretIdentifier The Secret Identifier to Secret
     * @param keyVaultWaitTime Amount of time to wait for a response from key vault.
     * @return
     */
    public Secret getSecret(URI secretIdentifier, Duration keyVaultWaitTime) {
        if (secretAsyncClient == null) {
            return null;
        }
        String[] tokens = secretIdentifier.getPath().split("/");

        String name = (tokens.length >= 3 ? tokens[2] : null);
        String version = (tokens.length >= 4 ? tokens[3] : null);

        return secretAsyncClient.getSecret(name, version).block(keyVaultWaitTime);
    }

}
