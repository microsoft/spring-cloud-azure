/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(FeatureManager.class)
public class FeatureManagementWebConfiguration {
    
    @Bean
    public FeatureManagerSnapshot featureManagerSnapshot(FeatureManager featureManager) {
        return new FeatureManagerSnapshot(featureManager);
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
    
    @Bean
    public FeatureHandler featureHandler(FeatureManager featureManager, FeatureManagerSnapshot snapshot, 
            FeatureManagementConfigProperties properties) {
        return new FeatureHandler(featureManager, snapshot, properties);
    }
    
    @Bean
    public FeatureConfig featureConfig(FeatureHandler featureHandler) {
        return new FeatureConfig(featureHandler);
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
