/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@PropertySource("classpath:appConfiguration.yaml")
@ConfigurationProperties(prefix = AppConfigProviderProperties.CONFIG_PREFIX)
public class AppConfigProviderProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.appconfiguration";
    
    @NotEmpty
    @Value("${version:1.0}")
    private String version;
    
    @NotEmpty
    @Value("${keyVaultWaitTime:5}")
    private int keyVaultWaitTime;

    /**
     * @return the apiVersion
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param apiVersion the apiVersion to set
     */
    public void setVersion(String apiVersion) {
        this.version = apiVersion;
    }

    /**
     * @return the keyVaultWaitTime
     */
    public int getKeyVaultWaitTime() {
        return keyVaultWaitTime;
    }

    /**
     * @param keyVaultWaitTime the keyVaultWaitTime to set
     */
    public void setKeyVaultWaitTime(int keyVaultWaitTime) {
        this.keyVaultWaitTime = keyVaultWaitTime;
    }

}
