/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("spring.cloud.azure.servicebus")
public class AzureServiceBusProperties {

    private String namespace;

    private String connectionString;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(namespace) && !StringUtils.hasText(connectionString)) {
            throw new IllegalArgumentException("Either 'spring.cloud.azure.servicebus.namespace' or " +
                    "'spring.cloud.azure.servicebus.connection-string' should be provided");
        }
    }
}
