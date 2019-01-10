/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class AzureConfigPropertySourceLocator implements PropertySourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigPropertySourceLocator.class);
    private static final String SPRING_APP_NAME_PROP = "spring.application.name";
    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";
    private static final String PATH_SPLITTER = "/";

    private final ConfigServiceOperations operations;
    private final AzureCloudConfigProperties properties;
    private final String profileSeparator;
    private final List<ConfigStore> configStores;

    public AzureConfigPropertySourceLocator(ConfigServiceOperations operations, AzureCloudConfigProperties properties) {
        this.operations = operations;
        this.properties = properties;
        this.profileSeparator = properties.getProfileSeparator();
        this.configStores = properties.getStores();
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return null;
        }

        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;

        String applicationName = this.properties.getName();
        if (!StringUtils.hasText(applicationName)) {
            applicationName = env.getProperty(SPRING_APP_NAME_PROP);
        }

        List<String> profiles = Arrays.asList(env.getActiveProfiles());

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        Collections.reverse(configStores); // Last store has highest precedence
        for (ConfigStore configStore : configStores) {
            addPropertySource(composite, configStore, applicationName, profiles);
        }

        return composite;
    }

    private void addPropertySource(CompositePropertySource composite, ConfigStore store, String applicationName,
                                   List<String> profiles) {
        /* Generate which contexts(key prefixes) will be used for key-value items search
           If key prefix is empty, default context is: application, current application name is: foo,
           active profile is: dev, profileSeparator is: _
           Will generate these contexts: /application/, /application_dev/, /foo/, /foo_dev/
        */
        List<String> contexts = new ArrayList<>();
        contexts.addAll(generateContexts(this.properties.getDefaultContext(), profiles, store));
        contexts.addAll(generateContexts(applicationName, profiles, store));

        // Reverse in order to add Profile specific properties earlier, and last profile comes first
        Collections.reverse(contexts);
        for (String sourceContext : contexts) {
            try {
                composite.addPropertySource(create(sourceContext, store));
                LOGGER.debug("PropertySource context [{}] is added.", sourceContext);
            } catch (Exception e) {
                if (properties.isFailFast()) {
                    LOGGER.error("Fail fast is set and there was an error reading configuration from Azure Config " +
                            "Service for " + sourceContext, e);
                    ReflectionUtils.rethrowRuntimeException(e);
                } else {
                    LOGGER.warn("Unable to load configuration from Azure Config Service for " + sourceContext, e);
                }
            }
        }
    }

    private List<String> generateContexts(String applicationName, List<String> profiles, ConfigStore configStore) {
        List<String> result = new ArrayList<>();
        if (!StringUtils.hasText(applicationName)) {
            return result; // Ignore null or empty application name
        }

        String prefix = configStore.getPrefix();

        String prefixedContext = propWithAppName(prefix, applicationName);
        result.add(prefixedContext + PATH_SPLITTER);
        profiles.forEach(profile -> result.add(propWithProfile(prefixedContext, profile)));

        return result;
    }

    private String propWithAppName(String prefix, String applicationName) {
        if (StringUtils.hasText(prefix)) {
            return prefix.startsWith(PATH_SPLITTER) ? prefix + PATH_SPLITTER + applicationName :
                    PATH_SPLITTER + prefix + PATH_SPLITTER + applicationName;
        }

        return PATH_SPLITTER + applicationName;
    }

    private String propWithProfile(String context, String profile) {
        return context + this.profileSeparator + profile + PATH_SPLITTER;
    }

    private AzureConfigPropertySource create(String context, ConfigStore store) {
        AzureConfigPropertySource propertySource = new AzureConfigPropertySource(context, operations, store);
        propertySource.initProperties();

        return propertySource;
    }
}
