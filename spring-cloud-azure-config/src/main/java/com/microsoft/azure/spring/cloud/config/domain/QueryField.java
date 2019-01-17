/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

public enum QueryField {
    ETAG("etag"), KEY("key"), LABEL("label"), CONTENT_TYPE("content_type"), VALUE("value"),
    LAST_MODIFIED("last_modified"), TAGS("tags");

    private final String name;

    QueryField(String name) {
        this.name = name;
    }

    public String getField() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}
