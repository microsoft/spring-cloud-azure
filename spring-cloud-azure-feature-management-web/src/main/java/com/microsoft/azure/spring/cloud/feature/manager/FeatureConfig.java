/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Adds the feature management handler to intercept all paths.
 */
@Configuration
public class FeatureConfig implements WebMvcConfigurer {
 
    @Autowired
    private FeatureHandler featureHandeler;
 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureHandeler)
          .addPathPatterns("/**");
    }
}
