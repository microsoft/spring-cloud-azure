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

    @JsonProperty("id")
    private String id;

    @JsonProperty("enabled")
    private boolean enabled = true;

    @JsonProperty("EnabledFor")
    private List<FeatureFilterEvaluationContext> enabledFor;
    
    @JsonProperty("enabledFor")
    private HashMap<Integer, HashMap<String, Object>> filterMapper;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the enabled
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
