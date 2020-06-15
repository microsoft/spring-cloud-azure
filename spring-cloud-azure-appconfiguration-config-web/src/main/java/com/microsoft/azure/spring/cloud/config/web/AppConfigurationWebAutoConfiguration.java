/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.web.refresh.AppConfigurationRefreshEndpoint;
import com.microsoft.azure.spring.cloud.config.web.refresh.ResetListener;
import com.microsoft.azure.spring.cloud.config.web.refreshbus.AppConfigurationRefreshBusEndpoint;
import com.microsoft.azure.spring.cloud.config.web.refreshbus.ResetBusListener;

@Configuration
@EnableConfigurationProperties(AppConfigurationProperties.class)
@RemoteApplicationEventScan
public class AppConfigurationWebAutoConfiguration {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationWebAutoConfiguration.class);

    // Refresh from appconfiguration-refresh

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties")
    public AppConfigurationRefreshEndpoint appConfigurationRefreshEndpoint(ContextRefresher contextRefresher,
            AppConfigurationProperties appConfiguration) {
        LOGGER.error("Creating Refresh Endpoint");
        return new AppConfigurationRefreshEndpoint(contextRefresher, appConfiguration);
    }

    @Bean
    @ConditionalOnClass(name = { "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
            "org.springframework.cloud.endpoint.RefreshEndpoint" })
    public ResetListener resetListener(AppConfigurationRefresh appConfigurationRefresh) {
        LOGGER.error("Creating Refresh Listener");
        return new ResetListener(appConfigurationRefresh);
    }

    // Refresh from appconfiguration-refresh-bus
    @Configuration
    @ConditionalOnClass(name = {
            "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
            "org.springframework.cloud.bus.BusProperties",
            "org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent",
            "org.springframework.cloud.endpoint.RefreshEndpoint" })
    public class AppConfigurationBusConfiguration {

        @Bean
        public AppConfigurationRefreshBusEndpoint appConfigurationRefreshBusEndpoint(ApplicationContext context,
                BusProperties bus,
                AppConfigurationProperties appConfiguration) {
            LOGGER.error("Creating Bus Endpoint");
            return new AppConfigurationRefreshBusEndpoint(context, bus.getId(), appConfiguration);
        }

        @Bean
        public ResetBusListener resetBusListener(AppConfigurationRefresh appConfigurationRefresh) {
            LOGGER.error("Creating Bus Listener");
            return new ResetBusListener(appConfigurationRefresh);
        }
    }

    // Pull based Refresh

    @Bean
    @ConditionalOnClass(RefreshEndpoint.class)
    public ConfigListener configListener(AppConfigurationRefresh appConfigurationRefresh) {
        return new ConfigListener(appConfigurationRefresh);
    }

}
