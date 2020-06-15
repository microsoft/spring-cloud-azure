/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.refreshbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;

@Component
public class ResetBusListener implements ApplicationListener<AppConfigurationCacheResetBusEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetBusListener.class);

    private AppConfigurationRefresh appConfigurationRefresh;

    public ResetBusListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(AppConfigurationCacheResetBusEvent event) {
        try {
            appConfigurationRefresh.resetCache(event.getEndpoint(), event.getTrigger());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
