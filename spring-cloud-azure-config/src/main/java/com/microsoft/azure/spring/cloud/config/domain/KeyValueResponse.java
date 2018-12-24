/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

import java.util.List;

/**
 * Response which stores key-value items, and other attributes, like next page token.
 */
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValueResponse {
    private List<KeyValueItem> items;

    public List<KeyValueItem> getItems() {
        return items;
    }

    public void setItems(List<KeyValueItem> items) {
        this.items = items;
    }
}
