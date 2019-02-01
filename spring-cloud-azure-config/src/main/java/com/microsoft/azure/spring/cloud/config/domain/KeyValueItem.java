/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValueItem {
    private String etag;

    private String key;

    private String value;

    private String label;

    @JsonProperty("content_type")
    private String contentType;

    private Map<String, String> tags;

    private boolean locked;

    @JsonProperty("last_modified")
    private Date lastModified;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "KeyValueItem(etag=" + this.getEtag() + ", key=" + this.getKey() + ", value=" + this.getValue() +
                ", label=" + this.getLabel() + ", contentType=" + this.getContentType() + ", tags=" + this.getTags() +
                ", locked=" + this.isLocked() + ", lastModified=" + this.getLastModified() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyValueItem item = (KeyValueItem) o;
        return Objects.equals(etag, item.etag) && Objects.equals(key, item.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(etag, key);
    }
}
