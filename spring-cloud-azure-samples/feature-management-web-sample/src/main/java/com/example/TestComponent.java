/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@Component
public class TestComponent {
    
    @Autowired
    private FeatureManager featureManager;
    
    public String test() {
        if(featureManager.isEnabled("Beta")) {
            return "Beta";
        }
        return "Original";
    }

}
