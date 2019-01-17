/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Options used for set up query to configuration store and control post process after data retrieved from remote.
 */
public class QueryOptions {
    private List<QueryField> fields = new ArrayList<>();
    private String keyNames;
    private String labels;
    private Range range;
    private QueryField sortField;

    public List<QueryField> getFields() {
        return fields;
    }

    public String getFieldsString() {
        return fields.stream().map(QueryField::toString).collect(Collectors.joining(","));
    }

    public String getKeyNames() {
        return keyNames;
    }

    public String getLabels() {
        return labels;
    }

    public List<String> getLabelList() {
        return StringUtils.hasText(labels) ? Arrays.asList(labels.split(",")) : new ArrayList<>();
    }

    public Range getRange() {
        return range;
    }

    public QueryField getSortField() {
        return sortField;
    }

    public QueryOptions withFields(List<QueryField> fields) {
        this.fields = fields;
        return this;
    }

    public QueryOptions withFields(QueryField field) {
        this.fields = Arrays.asList(field);
        return this;
    }

    public QueryOptions withKeyNames(String keyName) {
        this.keyNames = keyName;
        return this;
    }

    public QueryOptions withKeyNames(List<String> keyNames) {
        this.keyNames = String.join(",", keyNames);
        return this;
    }

    public QueryOptions withLabels(String labels) {
        if (!StringUtils.hasText(labels)) {
            return this;
        }
        return this.withLabels(Arrays.asList(labels.split(",")));
    }

    public QueryOptions withLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return this;
        }

        this.labels = labels.stream().filter(l -> StringUtils.hasText(l))
                .map(l -> l.trim()).collect(Collectors.joining(","));
        return this;
    }

    public QueryOptions withRange(int startItem, int endItem) {
        this.range = new Range(startItem, endItem);
        return this;
    }

    public QueryOptions withRange(Range range) {
        this.range = range;
        return this;
    }

    public QueryOptions withSortField(QueryField field) {
        this.sortField = field;
        return this;
    }
}
