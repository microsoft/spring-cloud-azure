/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.config;

import com.google.common.base.Strings;
import com.microsoft.azure.spring.cloud.context.core.api.CredentialSupplier;
import com.microsoft.azure.spring.cloud.context.core.api.Environment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

@Validated
@ConfigurationProperties("spring.cloud.azure")
public class AzureProperties implements CredentialSupplier {

    private String credentialFilePath;

    private String resourceGroup;

    private Environment environment = Environment.GLOBAL;

    private String region;

    private boolean autoCreateResources = false;

    private boolean msiEnabled = false;

    private String subscriptionId;

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                    "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (msiEnabled && Strings.isNullOrEmpty(subscriptionId)) {
            Assert.hasText(this.region, "When msi is enabled, spring.cloud.azure.subscription-id must be provided");
        }
    }

    @Override
    public String getCredentialFilePath() {
        return credentialFilePath;
    }

    public void setCredentialFilePath(String credentialFilePath) {
        this.credentialFilePath = credentialFilePath;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isAutoCreateResources() {
        return autoCreateResources;
    }

    public void setAutoCreateResources(boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }

    public boolean isMsiEnabled() {
        return msiEnabled;
    }

    public void setMsiEnabled(boolean msiEnabled) {
        this.msiEnabled = msiEnabled;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
