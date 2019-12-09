/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureSet;

public class FeatureSetTest {

    @Test
    public void multipartFeatureFlagNameTest() {
        HashMap<String, Object> features = new HashMap<String, Object>();
        LinkedHashMap<String, Object> featurePart = new LinkedHashMap<String, Object>();
        featurePart.put("A", true);
        features.put("Beta", featurePart);

        FeatureSet featureSet = new FeatureSet();
        featureSet.putAll(features);

        assertEquals(1, featureSet.getOnOff().size());
        assertTrue(featureSet.getOnOff().get("Beta.A"));
    }
    
    @Test
    public void invalidFeatureFlagTest() {
        HashMap<String, Object> features = new HashMap<String, Object>();
        LinkedHashMap<String, Object> featurePart = new LinkedHashMap<String, Object>();
        featurePart.put("A", 1);
        features.put("Beta", featurePart);
        
        FeatureSet featureSet = new FeatureSet();
        featureSet.putAll(features);

        assertEquals(0, featureSet.getOnOff().size());
        
    }

}
