/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Holds information on Feature Management properties and can check if a given feature is
 * enabled. Returns the same value in the same request.
 */
@Configuration
public class FeatureManagerSnapshot {

    @Autowired
    FeatureManager featureManager;

    @Autowired
    private HttpServletRequest request;

    /**
     * Checks to see if the feature is enabled. If enabled it check each filter, once a
     * single filter returns true it returns true. If no filter returns true, it returns
     * false. If there are no filters, it returns true. If feature isn't found it returns
     * false.
     * 
     * If isEnabled has already been called on this feature in this request, it will
     * return the same value as it did before.
     * 
     * @param feature Feature being checked.
     * @return state of the feature
     */
    public Boolean isEnabled(String feature) {
        if (request.getAttribute(feature) != null) {
            return (Boolean) request.getAttribute(feature);
        }
        Boolean enabled = featureManager.isEnabled(feature);
        request.setAttribute(feature, enabled);
        return enabled;
    }

}
