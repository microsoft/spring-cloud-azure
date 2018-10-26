/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.keyvault.KeyVaultClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a {@link KeyVaultPropertySource} instance based on application name and active profiles.
 */
@AllArgsConstructor
@Slf4j
public class KeyVaultPropertySourceLocator implements PropertySourceLocator {
    private static final String DEFAULT_APP_NAME_PROPERTY = "spring.application.name";
    private static final String PROPERTY_SOURCE_NAME = "azure-key-vault-config";
    // TODO: support Azure China, Azure Germany, Azure Government
    private static final String GLOBAL_KEY_VAULT_ENDPOINT_TEMPLATE = "https://%s.vault.azure.net";

    private static final String FAILED_TO_LOAD = "Failed to load configuration from Azure Key Vault: ";

    private final KeyVaultClient keyVaultClient;

    private final KeyVaultConfigProperties properties;

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return null;
        }

        if (!properties.isEnabled()) {
            return null;
        }

        final List<String> keyVaults = buildKeyVaultUrl((ConfigurableEnvironment) environment);

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        for (String keyVault : keyVaults) {
            try {
                composite.addPropertySource(new KeyVaultPropertySource(keyVault, keyVaultClient, keyVault));
            } catch (Exception ex) {
                if (this.properties.isFailFast()) {
                    log.error(FAILED_TO_LOAD + keyVault);
                    ReflectionUtils.rethrowRuntimeException(ex);
                } else {
                    log.warn(FAILED_TO_LOAD + keyVault, ex);
                }
            }
        }

        return composite;
    }

    // Key Vault name has the following formats:
    // - "{app name}-{profile}" when both of them are present
    // - "{app name}" when profile is empty
    private List<String> buildKeyVaultUrl(ConfigurableEnvironment environment) {

        String appName = properties.getName();
        if (StringUtils.isEmpty(appName)) {
            appName = environment.getProperty(DEFAULT_APP_NAME_PROPERTY);
        }

        String[] profiles = StringUtils.commaDelimitedListToStringArray(properties.getActiveProfiles());
        if (profiles == null || profiles.length == 0) {
            profiles = environment.getActiveProfiles();
        }

        if (profiles == null || profiles.length == 0) {
            return Arrays.asList(String.format(GLOBAL_KEY_VAULT_ENDPOINT_TEMPLATE, appName));
        }

        final String keyVaultNameTemplate = appName + "-%s";
        return Arrays.stream(profiles)
                .filter(StringUtils::hasText)
                .map(p -> String.format(keyVaultNameTemplate, p))
                .map(name -> String.format(GLOBAL_KEY_VAULT_ENDPOINT_TEMPLATE, name))
                .collect(Collectors.toList());
    }
}
