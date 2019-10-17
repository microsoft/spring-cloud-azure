/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.feature.manager.entities.Feature;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureSet;

/**
 * Holds information on Feature Management properties and can check if a given feature is
 * enabled.
 */
@Component("FeatureManager")
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManager {

    private static Logger logger = LoggerFactory.getLogger(FeatureManager.class);

    private FeatureSet featureManagement;

    // This is used to enable mapping both different types of read in.
    @SuppressWarnings("unused")
    @JsonProperty("featureSet")
    private FeatureSet featureSet;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ApplicationContext context;

    private FeatureManagementConfigProperties properties;

    public FeatureManager(FeatureManagementConfigProperties properties) {
        this.properties = properties;
    }

    /**
     * Checks to see if the feature is enabled. If enabled it check each filter, once a
     * single filter returns true it returns true. If no filter returns true, it returns
     * false. If there are no filters, it returns true. If feature isn't found it returns
     * false.
     * 
     * @param feature Feature being checked.
     * @return state of the feature
     */
    public boolean isEnabled(String feature) {
        boolean enabled = false;
        if (featureManagement == null || featureManagement.getFeatureManagement() == null ||
                featureManagement.getOnOff() == null) {
            return false;
        }

        Feature featureItem = featureManagement.getFeatureManagement().get(feature);
        Boolean boolFeature = featureManagement.getOnOff().get(feature);

        if (boolFeature != null) {
            return boolFeature;
        } else if (featureItem == null) {
            return false;
        }

        for (FeatureFilterEvaluationContext filter : featureItem.getEnabledFor()) {
            if (filter != null && filter.getName() != null) {
                try {
                    FeatureFilter featureFilter = (FeatureFilter) context.getBean(filter.getName());
                    enabled = featureFilter.evaluate(filter);
                } catch (NoSuchBeanDefinitionException e) {
                    logger.error("Was unable to find Filter " + filter.getName()
                            + ". Does the class exist and set as an @Component?");
                    if (properties.isFailFast()) {
                        String message = "Fail fast is set and a Filter was unable to be found.";
                        ReflectionUtils.rethrowRuntimeException(new FilterNotFoundException(message, e, filter));
                    }
                }
            }
            if (enabled) {
                return enabled;
            }
        }
        return enabled;
    }

    /**
     * @return the featureManagement
     */
    public FeatureSet getFeatureManagement() {
        return featureManagement;
    }

    /**
     * Converts LinkedHashMap to a Feature Set.
     * @param featureSet the featureSet to set
     */
    public void setFeatureManagement(LinkedHashMap<String, ?> featureSet) {
        this.featureManagement = mapper.convertValue(featureSet, FeatureSet.class);
    }

    /**
     * @param featureSet the featureSet to set
     */
    public void setFeatureSet(FeatureSet featureSet) {
        this.featureManagement = featureSet;
    }
}
