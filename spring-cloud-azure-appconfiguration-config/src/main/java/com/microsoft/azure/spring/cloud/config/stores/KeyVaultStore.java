/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import javax.validation.constraints.NotEmpty;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Validated
public class KeyVaultStore {
    
    @NotEmpty
    private String clientId;
    
    private String clientSecret;
    
    private Resource clientCertificate;

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * @param secret the secret to set
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * @return the clientCertificate
     */
    public Resource getClientCertificate() {
        return clientCertificate;
    }

    /**
     * @param clientCertificate the clientCertificate to set
     */
    public void setClientCertificate(Resource clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

}
