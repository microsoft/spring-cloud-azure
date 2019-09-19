/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.feature.management.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    @JsonProperty("id")
    private String id;

    @JsonAlias("EnabledFor")
    private List<FeatureFilterEvaluationContext> enabledFor;
    
    public Feature() {}
    
    public Feature(FeatureManagementItem featureItem) {
        this.id = featureItem.getId();
        this.enabledFor = featureItem.getConditions().getClientFilters();
    }

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

}
