/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class FeatureManagementWebConfiguration {
    
    @Bean
    public FeatureManagerSnapshot featureManagerSnapshot() {
        return new FeatureManagerSnapshot();
    }
    
    @Bean
    public FeatureHandler featureHandler(FeatureManager featureManager, FeatureManagerSnapshot snapshot, 
            FeatureManagementConfigProperties properties) {
        return new FeatureHandler(featureManager, snapshot, properties);
    }
    
    @Bean
    public FeatureConfig featureConfig(FeatureHandler featureHandler) {
        return new FeatureConfig(featureHandler);
    }

}
