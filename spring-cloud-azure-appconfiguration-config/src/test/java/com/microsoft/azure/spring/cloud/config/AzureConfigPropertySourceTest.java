/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.Feature;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureFilterEvaluationContext;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureSet;

public class AzureConfigPropertySourceTest {
    private static final AzureCloudConfigProperties TEST_PROPS = new AzureCloudConfigProperties();
    public static final List<KeyValueItem> TEST_ITEMS = new ArrayList<>();
    public static final List<KeyValueItem> FEATURE_ITEMS = new ArrayList<>();
    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1);
    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2);
    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3);

    private static final KeyValueItem featureItem = createItem(".appconfig.featureflag/", "Alpha", FEATURE_VALUE,
            FEATURE_LABEL);

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    private AzureConfigPropertySource propertySource;
    
    private static ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ConfigServiceOperations operations;
    
    private AzureCloudConfigProperties azureProperties;

    @BeforeClass
    public static void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING);

        TEST_ITEMS.add(item1);
        TEST_ITEMS.add(item2);
        TEST_ITEMS.add(item3);
        featureItem.setContentType(FEATURE_FLAG_CONTENT_TYPE);
        FEATURE_ITEMS.add(featureItem);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        azureProperties = new AzureCloudConfigProperties();
        azureProperties.setFailFast(true);
        propertySource = new AzureConfigPropertySource(TEST_CONTEXT, operations, null, null, azureProperties);
        when(operations.getKeys(any(), any())).thenReturn(TEST_ITEMS).thenReturn(FEATURE_ITEMS);
    }

    @Test
    public void testPropCanBeInitAndQueried() {
        try {
            propertySource.initProperties();
        } catch (IOException e) {
             fail("Failed Reading in Feature Flags");
        }

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = TEST_ITEMS.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);
        
        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() {
        KeyValueItem slashedProp = createItem(TEST_CONTEXT, TEST_SLASH_KEY, TEST_SLASH_VALUE, null);
        when(operations.getKeys(any(), any())).thenReturn(Arrays.asList(slashedProp)).thenReturn(FEATURE_ITEMS);

        try {
            propertySource.initProperties();
        } catch (IOException e) {
             fail("Failed Reading in Feature Flags");
        }

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(2);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }
    
    @Test
    public void testFeatureFlagCanBeInitedAndQueried() {
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(FEATURE_ITEMS);
        
        try {
            propertySource.initProperties();
        } catch (IOException e) {
             fail("Failed Reading in Feature Flags");
        }
        
        FeatureSet featureSet = new FeatureSet();
        Feature feature = new Feature();
        feature.setId("Alpha");
        ArrayList<FeatureFilterEvaluationContext> filters = new ArrayList<FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext ffec = new FeatureFilterEvaluationContext();
        ffec.setName("TestFilter");
        filters.add(ffec);
        feature.setEnabledFor(filters);
        featureSet.addFeature("Alpha", feature);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSet, LinkedHashMap.class);
        
        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }
}
