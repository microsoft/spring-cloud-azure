/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.net.UnknownHostException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostnameHelper {

    private static final String UNKNOWN_HOST_NAME = "Unknown-HostName";

    private static final String HOST_NAME = getNetworkHostname();

    private static String getNetworkHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) {
            return UNKNOWN_HOST_NAME;
        }
    }

    public static String getHostname() {
        return HOST_NAME;
    }
}
