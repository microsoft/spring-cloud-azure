/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "feature-management")
public class FeatureSet extends HashMap<String, Object> {

    private static final long serialVersionUID = -4565743301696064767L;

    private static Logger logger = LoggerFactory.getLogger(FeatureManager.class);

    public HashMap<String, Feature> featureManagement;

    public HashMap<String, Boolean> onOff;

    private ObjectMapper mapper = new ObjectMapper();

    public FeatureSet() {
        featureManagement = new HashMap<String, Feature>();
        onOff = new HashMap<String, Boolean>();
    }

    /**
     * @return the featureManagement
     */
    public HashMap<String, Feature> getFeatureManagement() {
        return featureManagement;
    }

    public void setFeatureManagement(HashMap<String, Feature> featureManagement) {
        this.featureManagement = featureManagement;
    }

    public void addFeature(Feature feature) {
        HashMap<String, Object> features = new HashMap<String, Object>();
        features.put(feature.getKey(), feature);
        addToFeatures(features, feature.getKey(), "");
    }

    @SuppressWarnings("unchecked")
    private void addToFeatures(Map<? extends String, ? extends Object> features, String key, String combined) {
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
                if (feature != null) {
                    feature.setKey(key);
                    featureManagement.put(key, feature);
                }
            }
        }
    }

    /**
     * @return the onOff
     */
    public HashMap<String, Boolean> getOnOff() {
        return onOff;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m == null) {
            return;
        }
        for (String key : m.keySet()) {
            addToFeatures(m, key, "");
        }
    }
}
