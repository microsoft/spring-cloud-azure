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
    private List<String> keyNames = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private Range range;
    private QueryField sortField;

    public List<QueryField> getFields() {
        return fields;
    }

    public String getFieldsString() {
        if (fields != null) {
            return fields.stream().map(QueryField::toString).collect(Collectors.joining(","));
        }
        return "";
    }

    public String getKeyNames() {
        return keyNames != null ? String.join(",", keyNames) : "";
    }

    public String getLabels() {
        return labels != null ? String.join(",", labels) : "";
    }

    public List<String> getLabelList() {
        return labels != null ?  labels : new ArrayList<>();
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
        if (StringUtils.hasText(keyName)) {
            this.withKeyNames(Arrays.asList(keyName.split(",")));
        }
        return this;
    }

    public QueryOptions withKeyNames(List<String> keyNames) {
        if (keyNames != null) {
            this.keyNames = keyNames.stream().filter(name -> StringUtils.hasText(name))
                    .map(name -> name.trim()).collect(Collectors.toList());
        }

        return this;
    }

    public QueryOptions withLabels(String labels) {
        if (StringUtils.hasText(labels)) {
            this.withLabels(Arrays.asList(labels.split(",")));
        }
        return this;
    }

    public QueryOptions withLabels(List<String> labels) {
        if (labels != null) {
            this.labels = labels.stream().filter(label -> StringUtils.hasText(label))
                    .map(label -> label.trim()).collect(Collectors.toList());
        }

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
