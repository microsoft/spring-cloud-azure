/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cloudfoundry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

enum AzureCfService {

    SERVICEBUS("servicebus", "azure-servicebus", ImmutableMap.of("ConnectionString", "connection-string")),
    EVENTHUB("eventhub", "azure-eventhub", ImmutableMap.of("ConnectionString", "connection-string")),
    STORAGE("storage", "azure-storage",
            ImmutableMap.of("StorageAccountName", "storage-account", "AccessKey", "access-key"));

    /**
     * Name of the Azure Cloud Foundry service in the VCAP_SERVICES JSON.
     */
    private String cfServiceName;

    /**
     * Name of the Spring Cloud Azure property.
     */
    private String azureServiceName;

    /**
     * Direct mapping of Azure service broker field names in VCAP_SERVICES JSON to Spring Cloud
     * Azure property names.
     */
    private Map<String, String> cfPropNameToAzure;

    AzureCfService(String cfServiceName, String azureServiceName, Map<String, String> cfPropNameToAzure) {
        this.cfServiceName = cfServiceName;
        this.azureServiceName = azureServiceName;
        this.cfPropNameToAzure = cfPropNameToAzure;
    }

    public String getCfServiceName() {
        return this.cfServiceName;
    }

    public Map<String, String> getCfPropNameToAzure() {
        return this.cfPropNameToAzure;
    }

    public String getAzureServiceName() {
        return this.azureServiceName;
    }
}
