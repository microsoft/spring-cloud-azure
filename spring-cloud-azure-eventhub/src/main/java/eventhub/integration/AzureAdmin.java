/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.storage.StorageAccount;
import org.springframework.util.Assert;

public class AzureAdmin {

    private final Azure azure;
    private final String resourceGroup;
    private final String region;

    public AzureAdmin(Azure azure, String resourceGroup, String region) {
        Assert.notNull(azure, "azure can't be null");
        Assert.hasText(resourceGroup, "resourceGroup can't be null or empty");
        Assert.hasText(region, "region can't be null or empty");
        this.azure = azure;
        this.resourceGroup = resourceGroup;
        this.region = region;
    }

    public EventHub getOrCreateEventHub(String namespace, String name) {
        EventHub eventHub = getEventHub(namespace, name);
        if (eventHub == null) {
            return createEventHub(namespace, name);
        }

        return eventHub;
    }

    public EventHub getEventHub(String namespace, String name) {
        return azure.eventHubs().getByName(resourceGroup, namespace, name);
    }

    public EventHub createEventHub(String namespace, String name) {
        EventHubNamespace eventHubNamespace = getOrCreateEventHubNamespace(namespace);

        return azure.eventHubs().define(name).withExistingNamespace(eventHubNamespace).create();
    }

    public EventHubNamespace getOrCreateEventHubNamespace(String namespace) {
        try {
            return azure.eventHubNamespaces().getByResourceGroup(resourceGroup, namespace);
        } catch (NullPointerException e) {
            // azure management api has no way to determine whether an eventhub namespace exists
            // Workaround for this is by catching NPE
            return azure.eventHubNamespaces().define(namespace).withRegion(region)
                        .withExistingResourceGroup(resourceGroup).create();
        }
    }

    public StorageAccount getOrCreateStorageAccount(String name) {
        StorageAccount storageAccount = getStorageAccount(name);
        if (storageAccount == null) {
            return createStorageAccount(name);
        }

        return storageAccount;
    }

    public StorageAccount getStorageAccount(String name) {
        return azure.storageAccounts().getByResourceGroup(resourceGroup, name);
    }

    public StorageAccount createStorageAccount(String name) {
        return azure.storageAccounts().define(name).withRegion(region).withExistingResourceGroup(resourceGroup)
                    .create();
    }

    private void createResourceGroupIfNotExisted() {
        if (!this.azure.resourceGroups().contain(resourceGroup)) {
            this.azure.resourceGroups().define(resourceGroup).withRegion(region).create();
        }
    }
}
