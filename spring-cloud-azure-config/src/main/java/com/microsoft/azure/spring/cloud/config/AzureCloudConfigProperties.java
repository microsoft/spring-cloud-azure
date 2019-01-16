/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.managed.identity.AzureManagedIdentityProperties;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@ConfigurationProperties(prefix = AzureCloudConfigProperties.CONFIG_PREFIX)
public class AzureCloudConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.config";
    public static final String LABEL_SEPARATOR = ",";

    private boolean enabled = true;

    private List<ConfigStore> stores = new ArrayList<>();

    @NotEmpty
    private String defaultContext = "application";

    // Alternative to Spring application name, if not configured, fallback to default Spring application name
    @Nullable
    private String name;

    @NestedConfigurationProperty
    private AzureManagedIdentityProperties managedIdentity;

    // Profile separator for the key name, e.g., /foo-app_dev/db.connection.key
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_@]+$")
    private String profileSeparator = "_";

    private boolean failFast = true;

    private Watch watch = new Watch();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<ConfigStore> getStores() {
        return stores;
    }

    public void setStores(List<ConfigStore> stores) {
        this.stores = stores;
    }

    public String getDefaultContext() {
        return defaultContext;
    }

    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public AzureManagedIdentityProperties getManagedIdentity() {
        return managedIdentity;
    }

    public void setManagedIdentity(AzureManagedIdentityProperties managedIdentity) {
        this.managedIdentity = managedIdentity;
    }

    public String getProfileSeparator() {
        return profileSeparator;
    }

    public void setProfileSeparator(String profileSeparator) {
        this.profileSeparator = profileSeparator;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public Watch getWatch() {
        return watch;
    }

    public void setWatch(Watch watch) {
        this.watch = watch;
    }

    @PostConstruct
    public void validateAndInit() {
        Assert.notEmpty(this.stores, "At least one config store has to be configured.");

        this.stores.forEach(store -> { Assert.isTrue(StringUtils.hasText(store.getName()) ||
                        StringUtils.hasText(store.getConnectionString()),
                    "Either configuration store name or connection string should be configured.");
            Assert.isTrue(!watch.enabled || store.watchedKeyIsValid(),
                    "Watched key should not be empty or equals asterisk(*) when watch is enabled.");
            store.validateAndInit();
        });

        int uniqueStoreSize = this.stores.stream().map(s -> s.getName()).distinct().collect(Collectors.toList()).size();
        Assert.isTrue(this.stores.size() == uniqueStoreSize, "Duplicate store name exists.");
    }

    class Watch {
        private boolean enabled = false;
        private int delay = 5000; /* milli-seconds */

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

    // Label values separated by comma in the Azure Config Service, can be empty
    @Nullable
    private String label;

    private String watchedKey; /* The single signal key to be watched, won't take effect if watch not enabled */

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

    public String getWatchedKey() {
        return watchedKey;
    }

    public void setWatchedKey(String watchedKey) {
        this.watchedKey = watchedKey;
    }

    @PostConstruct
    public void validateAndInit() {
        if (StringUtils.hasText(label)) {
            Assert.isTrue(!label.contains("*"), "Label must not contain asterisk(*).");
        }

        if (StringUtils.hasText(connectionString)) {
            String endpoint = ConnectionString.of(connectionString).getEndpoint();
            try {
                URI uri = new URI(endpoint);
                this.name = uri.getHost().split("\\.")[0];
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Endpoint in connection string is not a valid URI.", e);
            }
        }
    }

    public boolean watchedKeyIsValid() {
        return StringUtils.hasText(watchedKey) && !watchedKey.trim().equals("*");
    }
}
