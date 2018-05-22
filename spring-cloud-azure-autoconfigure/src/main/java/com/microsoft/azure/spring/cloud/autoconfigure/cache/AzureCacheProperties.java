/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.azure.cache")
public class AzureCacheProperties {
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
