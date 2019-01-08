/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used to build REST API uris for different service rest endpoints.
 */
public class RestAPIBuilder {
    public static final String KEY_VALUE_API = "/kv";
    public static final String NULL_LABEL = "%00"; // label=%00 matches null label
    private static final String KEY_PARAM = "key";
    private static final String LABEL_PARAM = "label";

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
     * Build REST API for kv request, depending on prefix and label is empty or not, different URIs will be built.
     *
     * <p>
     * e.g.,
     *   https://host.domain.io/kv
     *   https://host.domain.io/kv?key=abc
     *   https://host.domain.io/kv?key=abc*
     *   https://host.domain.io/kv?key=abc*&label=prod
     * </p>
     *
     * @param prefix is {@link Nullable}, key name prefix to be searched
     * @param labels is {@link Nullable}, {@link java.util.List} of key label values to be searched
     * @return valid full path of target REST API
     */
    public String buildKVApi(@Nullable String prefix, @Nullable List<String> labels) {
        this.withPath(KEY_VALUE_API);
        if (StringUtils.hasText(prefix)) {
            this.addParam(KEY_PARAM, prefix);
        }

        String label = NULL_LABEL;
        if (labels != null && !labels.isEmpty()) {
            labels = labels.stream().filter(l -> StringUtils.hasText(l))
                    .map(l -> l.trim()).distinct().collect(Collectors.toList());

            label = labels.isEmpty() ? NULL_LABEL : String.join(",", labels);
        }

        if (StringUtils.hasText(label)) {
            this.addParam(LABEL_PARAM, label);
        }

        return buildRequestUri();
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
