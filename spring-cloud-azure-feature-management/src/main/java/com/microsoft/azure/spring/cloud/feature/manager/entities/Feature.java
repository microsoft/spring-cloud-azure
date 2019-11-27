/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    @JsonProperty("key")
    private String key;

    @JsonProperty("enabledFor")
    private List<FeatureFilterEvaluationContext> enabledFor;
    
    @JsonProperty("EnabledFor")
    private HashMap<Integer, HashMap<String, Object>> filterMapper;

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the enabledFor
     */
    public List<FeatureFilterEvaluationContext> getEnabledFor() {
        return enabledFor;
    }

    /**
     * @param enabledFor the enabledFor to set
     */
    public void setEnabledFor(List<FeatureFilterEvaluationContext> enabledFor) {
        this.enabledFor = enabledFor;
    }
    
    public void setFilterMapper(HashMap<Integer, FeatureFilterEvaluationContext> filterMapper) {
        if (filterMapper == null) {
            return;
        }
        
        if (enabledFor == null) {
            enabledFor = new ArrayList<FeatureFilterEvaluationContext>();
        }
        
        for (Integer key: filterMapper.keySet()) {
            enabledFor.add(filterMapper.get(key));
        }
    }

}
