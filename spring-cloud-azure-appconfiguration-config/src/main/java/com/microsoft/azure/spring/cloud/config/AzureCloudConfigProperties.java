/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;
import com.microsoft.azure.spring.cloud.config.stores.KeyVaultStore;
import com.microsoft.azure.spring.cloud.context.core.config.AzureManagedIdentityProperties;

@Validated
@ConfigurationProperties(prefix = AzureCloudConfigProperties.CONFIG_PREFIX)
public class AzureCloudConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.appconfiguration";
    public static final String LABEL_SEPARATOR = ",";

    private boolean enabled = true;

    private List<ConfigStore> stores = new ArrayList<>();
    
    private List<KeyVaultStore> keyVaultStores = new ArrayList<>();

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
    
    private AzureEnvironment environment = AzureEnvironment.AZURE;

    /**
     * @return the environment
     */
    public AzureEnvironment getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(AzureEnvironment environment) {
        this.environment = environment;
    }

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
    
    public List<KeyVaultStore> getKeyVaultStores() {
        return keyVaultStores;
    }

    public void setKeyVaultStoresStores(List<KeyVaultStore> keyVaultStores) {
        this.keyVaultStores = keyVaultStores;
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

        this.stores.forEach(store -> { 
            Assert.isTrue(StringUtils.hasText(store.getName()) ||
            StringUtils.hasText(store.getConnectionString()),
            "Either configuration store name or connection string should be configured.");
            store.validateAndInit();
        });

        int uniqueStoreSize = this.stores.stream().map(s -> s.getName()).distinct().collect(Collectors.toList()).size();
        Assert.isTrue(this.stores.size() == uniqueStoreSize, "Duplicate store name exists.");
    }

    class Watch {
        private boolean enabled = false;
        private Duration delay = Duration.ofSeconds(30);

        public Watch() {
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getDelay() {
            return delay;
        }

        public void setDelay(Duration delay) {
            this.delay = delay;
        }
    }
}
