package com.microsoft.azure.spring.cloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AzureConfigPropertySourceLocator implements PropertySourceLocator {
    private static final String SPRING_APP_NAME_PROP = "spring.application.name";
    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";

    private final ConfigServiceOperations operations;
    private final AzureCloudConfigProperties properties;
    private List<String> contexts = new ArrayList<>();
    private final String separator;

    public AzureConfigPropertySourceLocator(ConfigServiceOperations operations, AzureCloudConfigProperties properties) {
        this.operations = operations;
        this.properties = properties;
        this.separator = properties.getSeparator();
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return null;
        }

        ConfigurableEnvironment env = (ConfigurableEnvironment)environment;

        String applicationName = this.properties.getName();
        if (!StringUtils.hasText(applicationName)) {
            applicationName = env.getProperty(SPRING_APP_NAME_PROP);
        }

        List<String> profiles = Arrays.asList(env.getActiveProfiles());

        /* Generate which contexts(key prefixes) will be used for key-value items search
           If key prefix is empty, default context is: application, current application name is: foo,
           active profile is: dev, separator is: /
           Will generate these contexts: /application, /application/dev, /foo, /foo/dev
         */
        generateContexts(this.properties.getDefaultContext(), profiles);
        generateContexts(applicationName, profiles);

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        Collections.reverse(this.contexts);
        for (String sourceContext : this.contexts) {
            composite.addPropertySource(create(sourceContext));
            log.debug("PropertySource context {} is added.", sourceContext);
        }

        return composite;
    }

    private void generateContexts(String applicationName, List<String> profiles) {
        String prefix = this.properties.getPrefix();

        String prefixedContext = propWithAppName(prefix, applicationName);
        this.contexts.add(prefixedContext + this.separator);
        profiles.forEach(profile -> this.contexts.add(propWithProfile(prefixedContext, profile)));
    }

    private String propWithAppName(String prefix, String applicationName) {
        if (StringUtils.hasText(prefix)) {
            return prefix.startsWith(this.separator) ? prefix + this.separator + applicationName :
                    this.separator + prefix + this.separator + applicationName;
        }

        return this.separator + applicationName;
    }

    private String propWithProfile(String context, String profile) {
        return context + separator + profile + separator;
    }

    private AzureConfigPropertySource create(String context) {
        AzureConfigPropertySource propertySource = new AzureConfigPropertySource(context, properties, operations);
        propertySource.initProperties();

        return propertySource;
    }
}
