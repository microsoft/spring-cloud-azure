/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigAccessKeys {
    @JsonProperty("value")
    private List<ConfigAccessKey> accessKeyList;

    public List<ConfigAccessKey> getAccessKeyList() {
        return accessKeyList;
    }

    public void setAccessKeyList(List<ConfigAccessKey> accessKeyList) {
        this.accessKeyList = accessKeyList;
    }
}

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ConfigAccessKey {
    private String connectionString;
    private String id;
    private String value;
    private boolean readOnly;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
