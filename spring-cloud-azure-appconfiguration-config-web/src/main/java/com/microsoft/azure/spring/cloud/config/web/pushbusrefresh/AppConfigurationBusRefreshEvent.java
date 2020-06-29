/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.pushbusrefresh;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

public class AppConfigurationBusRefreshEvent extends RemoteApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String endpoint;

    AppConfigurationBusRefreshEvent(String endpoint, AppConfigurationBusRefreshEndpoint source, String origin) {
        super("App Configuration Refresh Event", origin, null);
        this.endpoint = endpoint;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

}
