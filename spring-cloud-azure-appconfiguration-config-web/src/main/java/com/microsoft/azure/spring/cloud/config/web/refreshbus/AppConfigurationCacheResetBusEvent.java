/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.refreshbus;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;

public class AppConfigurationCacheResetBusEvent extends RemoteApplicationEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final String endpoint;

    private final AppConfigurationStoreTrigger trigger;

    public AppConfigurationCacheResetBusEvent(String endpoint, AppConfigurationRefreshBusEndpoint source, String origin,
            AppConfigurationStoreTrigger trigger) {
        super("Refresh Event", origin, null);
        this.endpoint = endpoint;
        this.trigger = trigger;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return the trigger
     */
    public AppConfigurationStoreTrigger getTrigger() {
        return trigger;
    }

}
