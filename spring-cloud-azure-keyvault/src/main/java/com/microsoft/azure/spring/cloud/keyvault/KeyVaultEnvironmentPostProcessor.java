/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Add {@link KeyVaultPropertySource} as last {@link org.springframework.context.annotation.PropertySource}
 * if this feature is enabled and related properties are provided
 * and {@link com.microsoft.azure.keyvault.KeyVaultClient} is on the classpath
 *
 * @author Warren Zhu
 */
public class KeyVaultEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultEnvironmentPostProcessor.class);
    private static final String AZURE_KEY_VAULT_ENABLED = "spring.cloud.azure.keyvault.enabled";
    private static final String AZURE_CLIENT_ID = "spring.cloud.azure.keyvault.client-id";
    private static final String AZURE_CLIENT_SECRET = "spring.cloud.azure.keyvault.client-secret";
    private static final String AZURE_KEY_VAULT_NAME = "spring.cloud.azure.keyvault.name";

    private static final Set<String> AZURE_KEY_VAULT_PROPERTIES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, AZURE_KEY_VAULT_NAME)));

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (shouldAddKeyVaultPropertySource(environment)) {
            LOGGER.info("Azure Key Vault enabled.");
            KeyVaultTemplate operation = new KeyVaultTemplate(environment.getProperty(AZURE_CLIENT_ID),
                    environment.getProperty(AZURE_CLIENT_SECRET));
            environment.getPropertySources()
                       .addLast(new KeyVaultPropertySource(operation, environment.getProperty(AZURE_KEY_VAULT_NAME)));
            LOGGER.info("KeyVaultPropertySource registered.");
        }
    }

    private static boolean shouldAddKeyVaultPropertySource(ConfigurableEnvironment environment) {
        return keyVaultEnabled(environment) && keyVaultPropertiesProvided(environment) && isKeyVaultClientAvailable();
    }

    private static boolean keyVaultEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(AZURE_KEY_VAULT_ENABLED, Boolean.class, true);
    }

    private static boolean keyVaultPropertiesProvided(ConfigurableEnvironment environment) {
        return AZURE_KEY_VAULT_PROPERTIES.stream().allMatch(environment::containsProperty);
    }

    private static boolean isKeyVaultClientAvailable() {
        return ClassUtils.isPresent("com.microsoft.azure.keyvault.KeyVaultClient",
                KeyVaultEnvironmentPostProcessor.class.getClassLoader());
    }
}
