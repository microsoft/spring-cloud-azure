/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import org.springframework.util.Assert;

/**
 * Config store access key resource in ARM, which provides access key data for the Azure Config Service.
 */
public class ConfigAccessKeyResource {
    static final String ARM_ENDPONT = "https://management.azure.com";
    private static final String CONFIG_RESOURCE_VERSION = "2018-02-01-preview";
    private static final String RESOURCE_ID_URL = "%s/subscriptions/%s/resourceGroups/%s/providers" +
            "/Microsoft.Azconfig/configurationStores/%s/listKeys?api-version=%s";

    private final String subscriptionId;
    private final String resourceGroupName;
    private final String configStoreName;

    public ConfigAccessKeyResource(AzureCloudConfigMSIProperties msiProperties) {
        Assert.notNull(msiProperties, "MSI configuration should not be null.");
        this.subscriptionId = msiProperties.getSubscriptionId();
        this.resourceGroupName = msiProperties.getResourceGroup();
        this.configStoreName = msiProperties.getConfigStore();
    }

    public String getResourceIdUrl() {
        Assert.hasText(subscriptionId, "Subscription id should not be null or empty.");
        Assert.hasText(resourceGroupName, "Resource group name should not be null or empty.");
        Assert.hasText(configStoreName, "Config store name should not be null or empty.");

        return String.format(RESOURCE_ID_URL, ARM_ENDPONT, subscriptionId, resourceGroupName,
                configStoreName, CONFIG_RESOURCE_VERSION);
    }

    public String getConfigStoreName() {
        return configStoreName;
    }
}
