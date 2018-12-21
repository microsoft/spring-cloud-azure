/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.resource;

import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionString {
    private static final String CONN_STRING_REGEXP = "Endpoint=https://([^;]+);Id=([^;]+);Secret=([^;]+)";
    public static final String ENDPOINT_ERR_MSG = String.format("Connection string does not follow format %s.",
            CONN_STRING_REGEXP);
    private static final Pattern CONN_STRING_PATTERN = Pattern.compile(CONN_STRING_REGEXP);
    public static final String NON_EMPTY_MSG = "%s property should not be null or empty in the connection string of " +
            "Azure Config Service.";

    private String endpoint;
    private String id;
    private String secret;

    public ConnectionString(String endpoint, String id, String secret) {
        this.endpoint = endpoint;
        this.id = id;
        this.secret = secret;
    }

    public static ConnectionString of(String connectionString) {
        Assert.hasText(connectionString, String.format("Connection string cannot be empty."));

        Matcher matcher = CONN_STRING_PATTERN.matcher(connectionString);
        if (!matcher.find()) {
            throw new IllegalStateException(ENDPOINT_ERR_MSG);
        }

        String endpoint = matcher.group(1);
        String id = matcher.group(2);
        String secret = matcher.group(3);

        Assert.hasText(endpoint, String.format(NON_EMPTY_MSG, "Endpoint"));
        Assert.hasText(id, String.format(NON_EMPTY_MSG, "Id"));
        Assert.hasText(secret, String.format(NON_EMPTY_MSG, "Secret"));

        return new ConnectionString(endpoint, id, secret);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }
}
