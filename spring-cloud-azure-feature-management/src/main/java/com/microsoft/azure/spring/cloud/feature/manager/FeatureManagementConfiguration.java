/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureSet;
import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.PercentageFilter;
import com.microsoft.azure.spring.cloud.feature.manager.feature.filters.TimeWindowFilter;

@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, FeatureSet.class })
public class FeatureManagementConfiguration {
    
    @Bean
    public FeatureManager featureManager(FeatureManagementConfigProperties properties, FeatureSet featureSet) {
        return new FeatureManager(properties, featureSet);
    }

    @Bean
    public PercentageFilter percentageFilter() {
        return new PercentageFilter();
    }

    @Bean
    public TimeWindowFilter timeWindowFilter() {
        return new TimeWindowFilter();
    }

}
