/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.feature.management.entity;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureSet {

    @JsonProperty("FeatureManagement")
    private HashMap<String, Feature> featureManagement;

    /**
     * @return the featureManagement
     */
    public HashMap<String, Feature> getFeatureManagement() {
        return featureManagement;
    }

    /**
     * @param featureManagement the featureManagement to set
     */
    public void setFeatureManagement(HashMap<String, Feature> featureManagement) {
        this.featureManagement = featureManagement;
    }
    
    public void addFeature(Feature feature) {
        if (featureManagement == null) {
            featureManagement = new HashMap<String, Feature>();
        }
        if (feature != null) {
            featureManagement.put(feature.getId(), feature);
        }
    }
}