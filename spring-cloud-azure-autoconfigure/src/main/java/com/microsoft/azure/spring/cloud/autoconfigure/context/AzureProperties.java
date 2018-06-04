/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.spring.cloud.context.core.CredentialSupplier;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.azure")
public class AzureProperties implements CredentialSupplier {

    private String credentialFilePath;
    private String resourceGroup;
    private String region;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCredentialFilePath() {
        return this.credentialFilePath;
    }

    public void setCredentialFilePath(String credentialFilePath) {
        this.credentialFilePath = credentialFilePath;
    }

    public String getResourceGroup() {
        return this.resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }
}
