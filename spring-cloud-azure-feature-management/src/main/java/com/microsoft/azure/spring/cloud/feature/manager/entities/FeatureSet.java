/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.entities;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureSet {
    
    @JsonProperty("FeatureManagement")
    private HashMap<String, Feature> featureManagement;
    
    private HashMap<String, Boolean> onOff;
    
    @JsonProperty("features")
    private HashMap<String, Object> features;
    
    private ObjectMapper mapper = new ObjectMapper();
    
    public FeatureSet() {
        featureManagement = new HashMap<String, Feature>();
        onOff = new HashMap<String, Boolean>();
        features = new HashMap<String, Object>();
    }
    
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
        featureManagement.put(feature.getId(), feature);
    }
    
    public void setFeatures(HashMap<String, Object> features) {
        for (String key: features.keySet()) {
            if (features.get(key) instanceof Boolean) {
                onOff.put(key, (Boolean) features.get(key));
            } else {
                Feature feature = mapper.convertValue(features.get(key), Feature.class);
                featureManagement.put(key, feature);
            }
        }
    }

    /**
     * @return the onOff
     */
    public HashMap<String, Boolean> getOnOff() {
        return onOff;
    }

    /**
     * @param onOff the onOff to set
     */
    public void setOnOff(HashMap<String, Boolean> onOff) {
        this.onOff = onOff;
    }
}
