/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@Component
public class ConfigListener implements ApplicationListener<ServletRequestHandledEvent>{

    private AzureCloudConfigWatch azureCloudConfigWatch;
    
    public ConfigListener(AzureCloudConfigWatch azureCloudConfigWatch) {
        System.out.println();
        this.azureCloudConfigWatch = azureCloudConfigWatch;
    }
    
    @Override
    public void onApplicationEvent(ServletRequestHandledEvent event) {
        azureCloudConfigWatch.refreshConfigurations();
    }

}
