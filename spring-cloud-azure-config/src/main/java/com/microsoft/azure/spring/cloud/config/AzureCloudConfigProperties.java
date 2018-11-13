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
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = AzureCloudConfigProperties.CONFIG_PREFIX)
public class AzureCloudConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.config";

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
    private String prefix;

    // Label value in the Azure Config Service, can be empty
    @Nullable
    private String label;

    // Path separator for the key name, e.g., /foo-app/dev/db.connection.key
    @NotEmpty
    private String separator = "/";

    public String getEndpoint() {
        return splitConnectionStr()[0].replaceFirst("Endpoint=", "");
    }

    public String getCredential() {
        return splitConnectionStr()[1].replaceFirst("Id=", "");
    }

    public String getSecret() {
        return splitConnectionStr()[2].replaceFirst("Secret=", "");
    }

    private String[] splitConnectionStr() {
        return connectionString.split(";");
    }
}
