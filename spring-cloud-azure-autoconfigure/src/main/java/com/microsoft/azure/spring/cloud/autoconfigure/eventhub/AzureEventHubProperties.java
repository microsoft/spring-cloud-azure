/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.azure.event.hub")
public class AzureEventHubProperties {
    private String namespace;
    private String checkpointStorageAccount;
    private String checkpointStorageAccountContainer;

    public String getCheckpointStorageAccountContainer() {
        return checkpointStorageAccountContainer;
    }

    public void setCheckpointStorageAccountContainer(String checkpointStorageAccountContainer) {
        this.checkpointStorageAccountContainer = checkpointStorageAccountContainer;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }
}
