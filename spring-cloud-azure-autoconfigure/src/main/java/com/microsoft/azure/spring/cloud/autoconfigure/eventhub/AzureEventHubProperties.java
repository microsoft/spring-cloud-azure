/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * @author Warren Zhu
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("spring.cloud.azure.eventhub")
public class AzureEventHubProperties {

    @NotEmpty(message = "spring.cloud.azure.eventhub.namespace must be provided")
    private String namespace;

    @NotEmpty(message = "spring.cloud.azure.eventhub.checkpoint-storage-account must be provided")
    private String checkpointStorageAccount;
}
