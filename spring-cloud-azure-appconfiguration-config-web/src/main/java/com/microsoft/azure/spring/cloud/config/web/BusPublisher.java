/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BusPublisher {
    
    private static final String ORIGIN_SERVICE = "azure-appconfiguration";
    
    private ApplicationEventPublisher context;
    
    public BusPublisher(ApplicationEventPublisher context) {
        this.context = context;
    }

    public void publish() {
        context.publishEvent(new RefreshRemoteApplicationEvent(this, ORIGIN_SERVICE, null));
    }

}
