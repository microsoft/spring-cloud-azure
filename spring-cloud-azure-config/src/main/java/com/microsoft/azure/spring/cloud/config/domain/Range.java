/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

import org.springframework.util.Assert;

public class Range {
    private static final String RANGE_PREFIX  = "items=";
    private static final String RANGE_QUERY = RANGE_PREFIX + "%d-%d";
    private final int startItem;
    private final int endItem;

    public Range(int startItem, int endItem) {
        Assert.isTrue(startItem <= endItem, "Start item should not be larger than end item.");
        this.startItem = startItem;
        this.endItem = endItem;
    }

    public String toString() {
        return String.format(RANGE_QUERY, startItem, endItem);
    }

    public int getStartItem() {
        return startItem;
    }

    public int getEndItem() {
        return endItem;
    }
}
