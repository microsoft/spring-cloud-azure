/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = AzureCloudConfigProperties.CONFIG_PREFIX)
public class AzureCloudConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.config";
    private static final String CONN_STRING_SPLITTER = ";";
    private static final String ENDPOINT_PREFIX = "endpoint=";
    private static final String ID_PREFIX = "id=";
    private static final String SECRET_PREFIX = "secret=";
    public static final String NON_EMPTY_MSG = "%s property should not be null or empty in the connection string of " +
            "Azure Config Service.";

    private boolean enabled = true;

    @NotEmpty
    private String connectionString;

    @NotEmpty
    private String defaultContext = "application";

    // Alternative to Spring application name, if not configured, fallback to default Spring application name
    @Nullable
    private String name;

    // Prefix for all properties, can be empty
    @Nullable
    @Pattern(regexp = "(/[a-zA-Z0-9.\\-_]+)*")
    private String prefix;

    // Label value in the Azure Config Service, can be empty
    @Nullable
    private String label;

    // Profile separator for the key name, e.g., /foo-app_dev/db.connection.key
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_@]+$")
    private String profileSeparator = "_";

    private boolean failFast = true;

    // Values extracted from connection string
    private String endpoint;
    private String id;
    private String secret;

    public String getEndpoint() {
        return endpoint;
    }

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }

    @PostConstruct
    private void validateAndInit() {
        String[] items = connectionString.split(CONN_STRING_SPLITTER);
        for (String item : items) {
            if (!StringUtils.hasText(item)) {
                continue;
            }

            String lowerCasedItem = item.toLowerCase();
            if (lowerCasedItem.startsWith(ENDPOINT_PREFIX)) {
                this.endpoint = item.substring(ENDPOINT_PREFIX.length());
            } else if (lowerCasedItem.startsWith(ID_PREFIX)) {
                this.id = item.substring(ID_PREFIX.length());
            } else if (lowerCasedItem.startsWith(SECRET_PREFIX)) {
                this.secret = item.substring(SECRET_PREFIX.length());
            }
        }

        Assert.hasText(this.endpoint, String.format(NON_EMPTY_MSG, "Endpoint"));
        Assert.hasText(this.id, String.format(NON_EMPTY_MSG, "Id"));
        Assert.hasText(this.secret, String.format(NON_EMPTY_MSG, "Secret"));
    }
}
