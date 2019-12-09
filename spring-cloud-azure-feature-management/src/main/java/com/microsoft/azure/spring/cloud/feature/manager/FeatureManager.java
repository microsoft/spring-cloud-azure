/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.microsoft.azure.spring.cloud.feature.manager.entities.Feature;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureSet;

/**
 * Holds information on Feature Management properties and can check if a given feature is
 * enabled.
 */
@Component("FeatureManager")
public class FeatureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private FeatureSet featureManagement;

    @Autowired
    private ApplicationContext context;

    private FeatureManagementConfigProperties properties;

    public FeatureManager(FeatureManagementConfigProperties properties, FeatureSet featureManagement) {
        this.properties = properties;
        this.featureManagement = featureManagement;
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
    public Future<Boolean> isEnabledAsync(String feature) {
        return CompletableFuture.supplyAsync(() -> checkFeatures(feature));
    }

    private boolean checkFeatures(String feature) {
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
                    enabled = CompletableFuture.supplyAsync(() -> featureFilter.evaluate(filter)).get();
                } catch (NoSuchBeanDefinitionException e) {
                    LOGGER.error("Was unable to find Filter " + filter.getName()
                            + ". Does the class exist and set as an @Component?");
                    if (properties.isFailFast()) {
                        String message = "Fail fast is set and a Filter was unable to be found.";
                        ReflectionUtils.rethrowRuntimeException(new FilterNotFoundException(message, e, filter));
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Filter {} was interrupted and unable to complete.", filter.getName());
                    ReflectionUtils.rethrowRuntimeException(e);
                } catch (ExecutionException e) {
                    LOGGER.error("Filter {} failed when retreving result.", filter.getName());
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
            if (enabled) {
                return enabled;
            }
        }
        return enabled;
    }
}
