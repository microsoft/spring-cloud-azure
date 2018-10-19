/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import lombok.AllArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Subclass of {@link KeyVaultCredentials}.
 */
@AllArgsConstructor
public class AadKeyVaultCredentials extends KeyVaultCredentials {
    // TODO: make it configurable.
    private static final int TIMEOUT_IN_SECONDS = 15;

    private AuthenticationExecutor authExecutor;

    @Override
    public String doAuthenticate(String authorization, String resource, String scope) {
        AuthenticationResult result;

        //Starts a service to fetch access token.
        ExecutorService service = null;
        try {
            service = Executors.newSingleThreadExecutor();
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);
            result = authExecutor.acquireToken(context, resource).get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to authenticate with Azure Key Vault.", ex);
        } finally {
            if (service != null) {
                service.shutdown();
            }
        }

        return result.getAccessToken();
    }
}
