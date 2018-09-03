/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@Getter
@Setter
@ConfigurationProperties("spring.cloud.azure.eventhub")
public class AzureEventHubProperties {
    private String namespace;
    private String checkpointStorageAccount;

    @PostConstruct
    public void validate() {
        Assert.hasText(namespace, "spring.cloud.azure.eventhub.namespace must be provided");
        Assert.hasText(checkpointStorageAccount, "spring.cloud.azure.eventhub.checkpoint-storage-account must be " +
                "provided");
    }
}
