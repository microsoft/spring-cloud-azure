/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This class is used to build REST API uris for different service rest endpoints.
 */
public class RestAPIBuilder {
    public static final String KEY_VALUE_API = "/kv";

    public static String buildKVApi(@NonNull String endpoint, @Nullable String prefix, @Nullable String label) {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.hasText(prefix)) {
            params.put("key", prefix);
        }

        if (StringUtils.hasText(label)) {
            params.put("label", label);
        }

        return buildRequestUri(endpoint, KEY_VALUE_API, params);
    }

    private static String buildRequestUri(@NonNull String endpoint, String path, Map<String, String> params) {
        Assert.notNull(endpoint, "Endpoint should not be null");
        Assert.hasText(path, "Request path should not be empty or null");

        StringBuilder builder = new StringBuilder();
        builder.append(endpoint);
        builder.append(path);

        if (params != null && params.size() > 0) {
            builder.append("?");

            String queryParams = String.join("&", params.entrySet().stream()
                    .map(p -> p.getKey() + "=" + p.getValue())
                    .collect(Collectors.toList()));

            builder.append(queryParams);
        }

        return builder.toString();
    }
}
