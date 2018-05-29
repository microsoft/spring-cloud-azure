/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AzureAdmin {

    @Autowired
    private Azure azure;

    @Autowired
    private AzureProperties azureProperties;

    @Autowired
    private AzureEventHubProperties eventHubProperties;

    public EventHub getOrCreateEventHub(String name) {
        EventHub eventHub = getEventHub(name);
        if (eventHub == null) {
            return createEventHub(name);
        }

        return eventHub;
    }

    public EventHub getEventHub(String name) {
        return azure.eventHubs().getByName(azureProperties.getResourceGroup(), eventHubProperties.getNamespace(), name);
    }

    public EventHub createEventHub(String name) {
        return azure.eventHubs().define(name).withExistingNamespace(azure.eventHubNamespaces().getByResourceGroup(
                azureProperties.getResourceGroup(), eventHubProperties.getNamespace())).create();
    }

    public StorageAccount getOrCreateStorageAccount(String name) {
        StorageAccount storageAccount = getStorageAccount(name);
        if (storageAccount == null) {
            return createStorageAccount(name);
        }

        return storageAccount;
    }

    public StorageAccount getStorageAccount(String name) {
        return azure.storageAccounts().getByResourceGroup(azureProperties.getResourceGroup(), name);
    }

    public StorageAccount createStorageAccount(String name) {
        return azure.storageAccounts().define(name).withRegion(azureProperties.getRegion())
                    .withExistingResourceGroup(azureProperties.getResourceGroup()).create();
    }
}
