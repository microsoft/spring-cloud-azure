/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.msi.AzureCloudConfigARMProperties;
import com.microsoft.azure.spring.cloud.config.msi.AzureCloudConfigMSIProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = AzureCloudConfigProperties.CONFIG_PREFIX)
public class AzureCloudConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.config";

    private boolean enabled = true;

    private List<ConfigStore> stores = new ArrayList<>();

    @NotEmpty
    private String defaultContext = "application";

    // Alternative to Spring application name, if not configured, fallback to default Spring application name
    @Nullable
    private String name;

    // Profile separator for the key name, e.g., /foo-app_dev/db.connection.key
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_@]+$")
    private String profileSeparator = "_";

    private boolean failFast = true;

    private Watch watch = new Watch();

    private AzureCloudConfigMSIProperties msi;

    private AzureCloudConfigARMProperties arm;

    // Initialized from the loaded properties
    private Map<String, ConnectionString> storeMap = new HashMap<>();

    @PostConstruct
    public void validateAndInit() {
        if (stores.isEmpty()) {
            return;
        }

        stores.forEach(store -> storeMap.put(store.getName(), ConnectionString.of(store.getConnectionString())));
    }

    class Watch {
        private boolean enabled = true;
        private int delay = 1000; /* milli-seconds */

        public Watch() {
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }
}

class ConfigStore {
    private String name; // Config store name
    @Nullable
    @Pattern(regexp = "(/[a-zA-Z0-9.\\-_]+)*")
    private String prefix;
    private String connectionString;
    // Label value in the Azure Config Service, can be empty
    @Nullable
    private String label;

    public ConfigStore() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

class ConnectionString {
    private static final String CONN_STRING_REGEXP = "Endpoint=(.*?);Id=(.*?);Secret=(.*?)";
    public static final String ENDPOINT_ERR_MSG = String.format("Connection string does not follow format %s.",
            CONN_STRING_REGEXP);
    private static final java.util.regex.Pattern CONN_STRING_PATTERN =
            java.util.regex.Pattern.compile(CONN_STRING_REGEXP);

    private String endpoint;
    private String id;
    private String secret;

    public ConnectionString(String endpoint, String id, String secret) {
        this.endpoint = endpoint;
        this.id = id;
        this.secret = secret;
    }

    static ConnectionString of(String connectionString) {
        Assert.hasText(connectionString, String.format("Connection string cannot be empty."));

        Matcher matcher = CONN_STRING_PATTERN.matcher(connectionString);
        if (!matcher.find()) {
            throw new IllegalStateException(String.format("Connection string does not follow format %s.",
                    CONN_STRING_REGEXP));
        }

        String endpoint = matcher.group(1);
        String id = matcher.group(2);
        String secret = matcher.group(3);

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
