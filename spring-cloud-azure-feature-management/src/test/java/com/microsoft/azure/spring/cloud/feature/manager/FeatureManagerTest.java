/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.feature.manager.entities.Feature;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureSet;

/**
 * Unit tests for FeatureManager.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeatureManagerTest {

    private static final String FEATURE_ID = "TestFeature";

    private static final String FILTER_NAME = "Filter1";

    private static final String PARAM_1_NAME = "param1";

    private static final String PARAM_1_VALUE = "testParam";

    @InjectMocks
    private FeatureManager featureManager;

    @Mock
    private ApplicationContext context;

    /**
     * Tests the conversion that takes place when data comes from
     * EnumerablePropertySource.
     */
    @Test
    public void loadFeatureManagerWithLinkedHashSet() {
        ObjectMapper mapper = new ObjectMapper();
        FeatureSet set = new FeatureSet();
        Feature f = new Feature();
        f.setId(FEATURE_ID);

        FeatureFilterEvaluationContext ffec = new FeatureFilterEvaluationContext();
        ffec.setName(FILTER_NAME);
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(PARAM_1_NAME, PARAM_1_VALUE);
        ffec.setParameters(parameters);

        ArrayList<FeatureFilterEvaluationContext> enabledFor = new ArrayList<FeatureFilterEvaluationContext>();
        enabledFor.add(ffec);
        f.setEnabledFor(enabledFor);

        set.addFeature(f);

        LinkedHashMap<String, ?> convertedValue = mapper.convertValue(set, LinkedHashMap.class);

        featureManager.setFeatureManagement(convertedValue);

        FeatureSet featureManagement = featureManager.getFeatureManagement();
        assertNotNull(featureManagement);
        assertNotNull(featureManagement.getFeatureManagement());
        assertEquals(1, featureManagement.getFeatureManagement().size());
        assertNotNull(featureManagement.getFeatureManagement().get(FEATURE_ID));
        Feature feature = featureManagement.getFeatureManagement().get(FEATURE_ID);
        assertEquals(FEATURE_ID, feature.getId());
        assertTrue(feature.getEnabled());
        assertEquals(1, feature.getEnabledFor().size());
        FeatureFilterEvaluationContext zeroth = feature.getEnabledFor().get(0);
        assertEquals(FILTER_NAME, zeroth.getName());
        assertEquals(1, zeroth.getParameters().size());
        assertEquals(PARAM_1_VALUE, zeroth.getParameters().get(PARAM_1_NAME));
    }

    @Test
    public void isEnabledFeatureNotFound() {
        FeatureSet featureSet = new FeatureSet();
        HashMap<String, Feature> features = new HashMap<String, Feature>();
        featureSet.setFeatureManagement(features);
        featureManager.setFeatureSet(featureSet);

        assertFalse(featureManager.isEnabled("Non Existed Feature"));
    }

    @Test
    public void isEnabledFeatureOff() {
        FeatureSet featureSet = new FeatureSet();
        HashMap<String, Feature> features = new HashMap<String, Feature>();
        Feature off = new Feature();
        off.setId("Off");
        off.setEnabled(false);
        features.put("Off", off);
        featureSet.setFeatureManagement(features);
        featureManager.setFeatureSet(featureSet);

        assertFalse(featureManager.isEnabled("Off"));
    }

    @Test
    public void isEnabledFeatureHasNoFilters() {
        FeatureSet featureSet = new FeatureSet();
        HashMap<String, Feature> features = new HashMap<String, Feature>();
        Feature noFilters = new Feature();
        noFilters.setId("NoFilters");
        noFilters.setEnabledFor(new ArrayList<FeatureFilterEvaluationContext>());
        features.put("NoFilters", noFilters);
        featureSet.setFeatureManagement(features);
        featureManager.setFeatureSet(featureSet);

        assertFalse(featureManager.isEnabled("NoFilters"));
    }

    @Test
    public void isEnabledON() {
        FeatureSet featureSet = new FeatureSet();
        HashMap<String, Feature> features = new HashMap<String, Feature>();
        Feature onFeature = new Feature();
        onFeature.setId("On");
        ArrayList<FeatureFilterEvaluationContext> filters = new ArrayList<FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.add(alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("On", onFeature);
        featureSet.setFeatureManagement(features);
        featureManager.setFeatureSet(featureSet);
        
        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOn());

        assertTrue(featureManager.isEnabled("On"));
    }
    
    @Component
    public class AlwaysOn implements FeatureFilter{

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return true;
        }

    }

}
