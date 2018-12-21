/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Iterator;

public class ConfigResourceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigResourceManager.class);
    private static final String AZ_CONFIG_RESOURCE_TYPE = "Microsoft.Azconfig/configurationStores";
    private Azure.Authenticated authenticated;

    public ConfigResourceManager(ConfigMSICredentials credentials) {
        Assert.notNull(credentials, "Credential token should not be null.");
        this.authenticated = Azure.authenticate(credentials);
    }

    /**
     * Search Azure Config Store which matches name {@code configStoreName}
     * @param configStoreName name of the Config Store to be searched
     * @return Tuple containing SubscriptionId and Resource Group Name information for {@code configStoreName},
     * return null if resource not found.
     */
    @Nullable
    public Tuple<String, String> findStore(String configStoreName) {
        Assert.hasText(configStoreName, "Config store name should not be null or empty.");

        LOGGER.debug("Search config name {} from Azure Configuration Service.", configStoreName);
        Subscriptions subscriptions = this.authenticated.subscriptions();
        Iterator<Subscription> subsIterator = subscriptions.list().iterator();
        while (subsIterator.hasNext()) {
            Subscription subscription = subsIterator.next();
            Azure subsAzure = this.authenticated.withSubscription(subscription.subscriptionId());

            GenericResources genericResources = subsAzure.genericResources();
            Iterator<GenericResource> genericResourceIterator = genericResources.list().iterator();
            while (genericResourceIterator.hasNext()) {
                GenericResource genericResource = genericResourceIterator.next();

                if (configStoreName.equals(genericResource.name())
                        && AZ_CONFIG_RESOURCE_TYPE.equals(genericResource.type())) {
                    // Found the resource
                    LOGGER.debug("Found resource with SubscriptionId=[{}], ResourceGroup=[{}] for config store " +
                            "[{}].", subscription.subscriptionId(), genericResource.resourceGroupName(),
                            configStoreName);
                    Tuple<String, String> resourceInfo = Tuple.of(subscription.subscriptionId(),
                            genericResource.resourceGroupName());

                    return resourceInfo;
                }
            }
        }

        LOGGER.debug("No config store with name {} exists.", configStoreName);
        return null;
    }
}
