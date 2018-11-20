/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AzureConfigPropertySourceLocator implements PropertySourceLocator {
    private static final String SPRING_APP_NAME_PROP = "spring.application.name";
    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";
    private static final String PATH_SPLITTER = "/";

    private final ConfigServiceOperations operations;
    private final AzureCloudConfigProperties properties;
    private List<String> contexts = new ArrayList<>();
    private final String profileSeparator;

    public AzureConfigPropertySourceLocator(ConfigServiceOperations operations, AzureCloudConfigProperties properties) {
        this.operations = operations;
        this.properties = properties;
        this.profileSeparator = properties.getProfileSeparator();
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

        /* Generate which contexts(key prefixes) will be used for key-value items search
           If key prefix is empty, default context is: application, current application name is: foo,
           active profile is: dev, profileSeparator is: _
           Will generate these contexts: /application/, /application_dev/, /foo/, /foo_dev/
         */
        this.contexts.addAll(generateContexts(this.properties.getDefaultContext(), profiles));
        this.contexts.addAll(generateContexts(applicationName, profiles));

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        // Reverse in order to add Profile specific properties earlier
        Collections.reverse(this.contexts);
        for (String sourceContext : this.contexts) {
            try {
                composite.addPropertySource(create(sourceContext));
                log.debug("PropertySource context {} is added.", sourceContext);
            } catch (Exception e) {
                if (properties.isFailFast()) {
                    log.error("Fail fast is set and there was an error reading configuration from Azure Config " +
                            "Service for " + sourceContext, e);
                    ReflectionUtils.rethrowRuntimeException(e);
                } else {
                    log.warn("Unable to load configuration from Azure Config Service for " + sourceContext, e);
                }
            }
        }

        return composite;
    }

    private List<String> generateContexts(String applicationName, List<String> profiles) {
        List<String> result = new ArrayList<>();
        String prefix = this.properties.getPrefix();

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

    private AzureConfigPropertySource create(String context) {
        AzureConfigPropertySource propertySource = new AzureConfigPropertySource(context, properties, operations);
        propertySource.initProperties();

        return propertySource;
    }
}
