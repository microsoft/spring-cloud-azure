/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureSet {

    private static Logger logger = LoggerFactory.getLogger(FeatureManager.class);

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
    public void setFeatureManagement(HashMap<String, Object> featureManagement) {
        setFeatures(featureManagement);
    }

    public void addFeature(Feature feature) {
        featureManagement.put(feature.getKey(), feature);
    }

    public void setFeatures(HashMap<String, Object> features) {
        if (features == null) {
            return;
        }
        for (String key : features.keySet()) {
            addToFeatures(features, key, "");
        }
    }

    @SuppressWarnings("unchecked")
    private void addToFeatures(HashMap<String, Object> features, String key, String combined) {
        Object featureKey = features.get(key);
        if (!combined.isEmpty() && !combined.endsWith(".")) {
            combined += ".";
        }
        if (featureKey instanceof Boolean) {
            onOff.put(combined + key, (Boolean) featureKey);
        } else {
            Feature feature = null;
            try {
                feature = mapper.convertValue(featureKey, Feature.class);
            } catch (IllegalArgumentException e) {
                logger.error("Found invalid feature {} with value {}.", combined + key, featureKey.toString());
            }

            // When coming from a file "feature.flag" is not a possible flag name
            if (feature != null && feature.getEnabledFor() == null && feature.getKey() == null) {
                if (LinkedHashMap.class.isAssignableFrom(featureKey.getClass())) {
                    features = (LinkedHashMap<String, Object>) featureKey;
                    for (String fKey : features.keySet()) {
                        addToFeatures(features, fKey, combined + key);
                    }
                }
            } else {
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
