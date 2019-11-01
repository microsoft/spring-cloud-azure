/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

public class AzureConfigPropertySourceLocator implements PropertySourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigPropertySourceLocator.class);

    private static final String SPRING_APP_NAME_PROP = "spring.application.name";

    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";

    private static final String PATH_SPLITTER = "/";

    private final AzureCloudConfigProperties properties;

    private final String profileSeparator;

    private final List<ConfigStore> configStores;

    private final Map<String, List<String>> storeContextsMap = new ConcurrentHashMap<>();

    private AppConfigProviderProperties appProperties;

    private ClientStore clients;

    public AzureConfigPropertySourceLocator(AzureCloudConfigProperties properties,
            AppConfigProviderProperties appProperties, ClientStore clients) {
        this.properties = properties;
        this.appProperties = appProperties;
        this.profileSeparator = properties.getProfileSeparator();
        this.configStores = properties.getStores();
        this.clients = clients;
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

        Iterator<ConfigStore> configStoreIterator = configStores.iterator();
        // Feature Management needs to be set in the last config store.
        while (configStoreIterator.hasNext()) {
            ConfigStore configStore = configStoreIterator.next();
            addPropertySource(composite, configStore, applicationName, profiles, storeContextsMap,
                    !configStoreIterator.hasNext());
        }

        return composite;
    }

    public Map<String, List<String>> getStoreContextsMap() {
        return this.storeContextsMap;
    }

    /**
     * Adds a new Property Source
     * 
     * @param composite PropertySource being added
     * @param store Config Store the PropertySource is being generated from
     * @param applicationName Name of the application
     * @param profiles Active profiles in the Store
     * @param storeContextsMap the Map storing the storeName -> List of contexts map
     * @param initFeatures determines if Feature Management is set in the PropertySource.
     * When generating more than one it needs to be in the last one.
     */
    private void addPropertySource(CompositePropertySource composite, ConfigStore store, String applicationName,
            List<String> profiles, Map<String, List<String>> storeContextsMap, boolean initFeatures) {
        /*
         * Generate which contexts(key prefixes) will be used for key-value items search
         * If key prefix is empty, default context is: application, current application
         * name is: foo, active profile is: dev, profileSeparator is: _ Will generate
         * these contexts: /application/, /application_dev/, /foo/, /foo_dev/
         */
        List<String> contexts = new ArrayList<>();
        contexts.addAll(generateContexts(this.properties.getDefaultContext(), profiles, store));
        contexts.addAll(generateContexts(applicationName, profiles, store));

        // There is only one Feature Set for all AzureConfigPropertySources
        FeatureSet featureSet = new FeatureSet();

        // Reverse in order to add Profile specific properties earlier, and last profile
        // comes first
        Collections.reverse(contexts);
        for (String sourceContext : contexts) {
            try {
                List<AzureConfigPropertySource> sourceList = create(sourceContext, store, storeContextsMap,
                        initFeatures, featureSet);
                sourceList.forEach(composite::addPropertySource);
                LOGGER.debug("PropertySource context [{}] is added.", sourceContext);
            } catch (Exception e) {
                if (properties.isFailFast()) {
                    LOGGER.error("Fail fast is set and there was an error reading configuration from Azure Config " +
                            "Service for " + sourceContext);
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
            return prefix.startsWith(PATH_SPLITTER) ? prefix + PATH_SPLITTER + applicationName
                    : PATH_SPLITTER + prefix + PATH_SPLITTER + applicationName;
        }

        return PATH_SPLITTER + applicationName;
    }

    private String propWithProfile(String context, String profile) {
        return context + this.profileSeparator + profile + PATH_SPLITTER;
    }

    /**
     * Creates a new set of AzureConfigProertySources, 1 per Label.
     * 
     * @param context Context of the application, part of uniquely define a PropertySource
     * @param store Config Store the PropertySource is being generated from
     * @param storeContextsMap the Map storing the storeName -> List of contexts map
     * @param initFeatures determines if Feature Management is set in the PropertySource.
     * When generating more than one it needs to be in the last one.
     * @return a list of AzureConfigPropertySources
     */
    private List<AzureConfigPropertySource> create(String context, ConfigStore store,
            Map<String, List<String>> storeContextsMap, boolean initFeatures, FeatureSet featureSet) throws Exception {
        List<AzureConfigPropertySource> sourceList = new ArrayList<>();

        try {
            for (String label : store.getLabels()) {
                AzureConfigPropertySource propertySource = new AzureConfigPropertySource(context, store.getName(),
                        label,
                        properties, appProperties, clients);

                propertySource.initProperties(featureSet);
                if (initFeatures) {
                    propertySource.initFeatures(featureSet);
                }
                sourceList.add(propertySource);
                putStoreContext(store.getName(), context, storeContextsMap);
            }
        } catch (Exception e) {
            delayException();
            throw e;
        }

        return sourceList;
    }

    /**
     * Put certain context to the store contexts map
     * @param storeName the name of the configuration store
     * @param context the context text for the PropertySource, e.g., "/application"
     * @param storeContextsMap the Map storing the storeName -> List of contexts map
     */
    private void putStoreContext(String storeName, String context,
            @NonNull Map<String, List<String>> storeContextsMap) {
        if (!StringUtils.hasText(context) || !StringUtils.hasText(storeName)) {
            return;
        }

        List<String> contexts = storeContextsMap.get(storeName);
        if (contexts == null) {
            contexts = Lists.newArrayList(context);
        } else {
            contexts.add(context);
        }

        storeContextsMap.put(storeName, contexts);
    }

    private void delayException() {
        Date currentDate = new Date();
        Date maxRetryDate = DateUtils.addSeconds(appProperties.getStartDate(),
                appProperties.getPrekillTime());
        if (currentDate.before(maxRetryDate)) {
            long diffInMillies = Math.abs(maxRetryDate.getTime() - currentDate.getTime());
            try {
                Thread.sleep(diffInMillies);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait before fast fail.");
            }
        }
    }
}
