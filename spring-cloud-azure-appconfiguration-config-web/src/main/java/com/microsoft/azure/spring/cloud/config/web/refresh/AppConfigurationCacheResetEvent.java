/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.refresh;

import org.springframework.context.ApplicationEvent;

import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;

public class AppConfigurationCacheResetEvent extends ApplicationEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final String endpoint;

    private final AppConfigurationStoreTrigger trigger;

    public AppConfigurationCacheResetEvent(String endpoint, AppConfigurationStoreTrigger trigger) {
        super(endpoint + "/" + trigger.toString());
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
