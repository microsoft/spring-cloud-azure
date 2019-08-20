/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.Date;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public class CachedKey {

    private String key;

    private String etag;

    private String value;

    private String storeName;
    
    private String contentType;

    private Date lastUpdated;
    
    public CachedKey(ConfigurationSetting item, String storeName, Date time) {
        key = item.key();
        etag = item.etag();
        value = item.value();
        this.storeName = storeName;
        lastUpdated = time;
        this.contentType = item.contentType();
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the etag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * @param etag the etag to set
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
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
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
