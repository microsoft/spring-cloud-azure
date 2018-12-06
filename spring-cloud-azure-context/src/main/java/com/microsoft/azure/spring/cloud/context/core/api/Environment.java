/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.api;

public enum Environment {
    GLOBAL("core.windows.net"),
    CHINA("core.chinacloudapi.cn");

    private final String storageEndpoint;

    Environment(String storageEndpoint) {
        this.storageEndpoint = storageEndpoint;
    }

    public String getStorageEndpoint() {
        return storageEndpoint;
    }
}
