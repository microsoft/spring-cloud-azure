/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AzureConfigPropertySourceTest {
    private static final AzureCloudConfigProperties TEST_PROPS = new AzureCloudConfigProperties();
    public static final List<KeyValueItem> TEST_ITEMS = new ArrayList<>();
    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1);
    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2);
    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3);
    private static final KeyValueItem featureItem = createItem(TEST_CONTEXT, FEATURE_KEY, FEATURE_VALUE, FEATURE_LABEL);

    private AzureConfigPropertySource propertySource;

    @Mock
    private ConfigServiceOperations operations;

    @BeforeClass
    public static void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING);

        TEST_ITEMS.add(item1);
        TEST_ITEMS.add(item2);
        TEST_ITEMS.add(item3);
        TEST_ITEMS.add(featureItem);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        propertySource = new AzureConfigPropertySource(TEST_CONTEXT, operations, null, null);
        when(operations.getKeys(any(), any())).thenReturn(TEST_ITEMS);
    }

    @Test
    public void testPropCanBeInitAndQueried() {
        propertySource.initProperties();

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = TEST_ITEMS.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() {
        KeyValueItem slashedProp = createItem(TEST_CONTEXT, TEST_SLASH_KEY, TEST_SLASH_VALUE, null);
        when(operations.getKeys(any(), any())).thenReturn(Arrays.asList(slashedProp));

        propertySource.initProperties();

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(2);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }
}
