/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This class is used to build REST API uris for different service rest endpoints.
 */
public class RestAPIBuilder {
    public static final String KEY_VALUE_API = "/kv";

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
     * @param label  is {@link Nullable}, key label value to be searched
     * @return valid full path of target REST API
     */
    public String buildKVApi(@Nullable String prefix, @Nullable String label) {
        this.withPath(KEY_VALUE_API);
        if (StringUtils.hasText(prefix)) {
            this.addParam("key", prefix);
        }

        label = StringUtils.isEmpty(label) ? "%00" : label; // label=%00 matches null label
        if (StringUtils.hasText(label)) {
            this.addParam("label", label);
        }

        return buildRequestUri();
    }

    private String buildRequestUri() {
        Assert.hasText(endpoint, "Endpoint should not be empty or null");
        Assert.hasText(path, "Request path should not be empty or null");

        StringBuilder builder = new StringBuilder();
        builder.append(endpoint);
        builder.append(path);

        if (params != null && params.size() > 0) {
            // append query params, example: "?param1=value1&param2=value2"
            builder.append("?");

            String queryParams = String.join("&", params.entrySet().stream()
                    .map(p -> p.getKey() + "=" + p.getValue())
                    .collect(Collectors.toList()));

            builder.append(queryParams);
        }

        return builder.toString();
    }
}
