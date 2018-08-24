/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault;

import java.util.Collection;

/**
 * Azure Key Vault related operation
 *
 * @author Warren Zhu
 */
public interface KeyVaultOperation {

    /**
     * Get secret from local cache. If not existed, fetch from remote service
     */
    default String getSecret(String keyVaultName, String secretName) {
        return this.getSecret(keyVaultName, secretName, true);
    }

    String getSecret(String keyVaultName, String secretName, boolean fromCache);

    /**
     * List secret names from local cache. If not existed, fetch from remote service
     */
    default Collection<String> listSecrets(String keyVaultName) {
        return this.listSecrets(keyVaultName, true);
    }

    Collection<String> listSecrets(String keyVaultName, boolean fromCache);

}
