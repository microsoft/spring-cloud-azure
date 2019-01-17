/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used to build REST API uris for different service rest endpoints.
 */
public class RestAPIBuilder {
    public static final String KEY_VALUE_API = "/kv";
    public static final String REVISIONS_API = "/revisions";
    public static final String NULL_LABEL = "%00"; // label=%00 matches null label
    private static final String KEY_PARAM = "key";
    private static final String LABEL_PARAM = "label";
    private static final String FIELDS_PARAM = "fields";

    private String endpoint;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public RestAPIBuilder() {
    }

    public RestAPIBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public RestAPIBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public RestAPIBuilder addParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    /**
     * Build REST API for kv request, depending on the query options, the URI built can be different.
     *
     * <p>
     * e.g.,
     *   https://host.domain.io/kv
     *   https://host.domain.io/kv?key=abc
     *   https://host.domain.io/kv?key=abc*
     *   https://host.domain.io/kv?key=abc*&label=prod
     * </p>
     *
     * @param options is {@link NonNull}, query options used to build the KV API
     * @return valid full path of target REST API
     */
    public String buildKVApi(@NonNull QueryOptions options) {
        this.withPath(KEY_VALUE_API);
        this.buildParams(options);

        return buildRequestUri();
    }

    public String buildRevisionsApi(@NonNull QueryOptions options) {
        this.withPath(REVISIONS_API);
        this.buildParams(options);

        return buildRequestUri();
    }

    private void buildParams(QueryOptions options) {
        String keyNames = options.getKeyNames();
        if (StringUtils.hasText(keyNames)) {
            this.addParam(KEY_PARAM, keyNames);
        }

        String label = NULL_LABEL;
        if (StringUtils.hasText(options.getLabels())) {
            label = options.getLabels();
        }

        if (StringUtils.hasText(label)) {
            this.addParam(LABEL_PARAM, label);
        }

        if (options.getFields() != null) {
            this.addParam(FIELDS_PARAM, options.getFieldsString());
        }
    }

    /**
     * User is responsible to configure the required endpoint, path and params ahead
     * @return valid full path of target REST API
     */
    public String buildKVApi() {
        return buildRequestUri();
    }

    private String buildRequestUri() {
        Assert.hasText(endpoint, "Endpoint should not be empty or null");
        Assert.hasText(path, "Request path should not be empty or null");

        StringBuilder builder = new StringBuilder();
        builder.append(endpoint);
        builder.append(path);

        if (params != null && !params.isEmpty()) {
            // append query params, example: "?param1=value1&param2=value2"
            if (!path.contains("?")) {
                builder.append("?");
            }

            String queryParams = String.join("&", params.entrySet().stream()
                    .map(p -> p.getKey() + "=" + p.getValue())
                    .collect(Collectors.toList()));

            builder.append(queryParams);
        }

        return builder.toString();
    }
}
