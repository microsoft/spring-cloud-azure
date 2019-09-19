/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.Date;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public class CachedKey extends ConfigurationSetting{

    private String storeName;

    private Date lastUpdated;
    
    private final ConfigurationSetting setting;
    
    public CachedKey(ConfigurationSetting setting, String storeName, Date time) {
        this.storeName = storeName;
        this.lastUpdated = time;
        this.key(setting.key());
        this.label(setting.label());
        this.value(setting.value());
        this.contentType(setting.contentType());
        this.etag(setting.etag());
        this.tags(setting.tags());
        this.setting = setting;
    }

    /**
     * @return the storeName
     */
    public String getStoreName() {
        return storeName;
    }

    /**
     * @param storeName the storeName to set
     */
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    /**
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return the setting
     */
    public ConfigurationSetting getSetting() {
        return setting;
    }

}
