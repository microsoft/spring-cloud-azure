/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_FLAG_CONTENT_TYPE;
import static com.microsoft.azure.spring.cloud.config.Constants.KEY_VAULT_CONTENT_TYPE;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FEATURE_LABEL;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FEATURE_VALUE;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONTEXT;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_2;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_3;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_VAULT_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_LABEL_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_LABEL_2;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_LABEL_3;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_LABEL_VAULT_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_SLASH_KEY;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_SLASH_VALUE;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_2;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_3;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_VAULT_1;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.azure.identity.credential.ChainedTokenCredential;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.Feature;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureFilterEvaluationContext;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureSet;

import reactor.core.publisher.Mono;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AzureConfigPropertySource.class})
public class AzureConfigPropertySourceTest {
    private static final String EMPTY_CONTENT_TYPE = "";
    
    private static final AzureCloudConfigProperties TEST_PROPS = new AzureCloudConfigProperties();

    public static List<KeyValueItem> testItems = new ArrayList<>();

    public static final List<KeyValueItem> FEATURE_ITEMS = new ArrayList<>();

    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
            EMPTY_CONTENT_TYPE);

    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2,
            EMPTY_CONTENT_TYPE);

    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
            EMPTY_CONTENT_TYPE);
    
    private static final KeyValueItem item3Null = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
            null);

    private static final KeyValueItem featureItem = createItem(".appconfig.featureflag/", "Alpha", FEATURE_VALUE,
            FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);
    
    private static final KeyValueItem featureItemNull = createItem(".appconfig.featureflag/", "Alpha", FEATURE_VALUE,
            FEATURE_LABEL, null);

    private static final KeyValueItem keyVaultItem = createItem(TEST_CONTEXT, TEST_KEY_VAULT_1, TEST_VALUE_VAULT_1,
            TEST_LABEL_VAULT_1, KEY_VAULT_CONTENT_TYPE);

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private AzureConfigPropertySource propertySource;

    private static ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ConfigServiceOperations operations;

    @Mock
    private ChainedTokenCredential keyVaultCredential;
    
    @Mock
    private SecretClientBuilder secretClientBuilder;
    
    @Mock
    private SecretAsyncClient secretAsyncClient;
    
    @Mock
    private Mono<Secret> monoSecret;

    private AzureCloudConfigProperties azureProperties;
    
    private AppConfigProviderProperties appProperties;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING);

        keyVaultItem.setContentType(KEY_VAULT_CONTENT_TYPE);
        
        FEATURE_ITEMS.add(featureItem);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        azureProperties = new AzureCloudConfigProperties();
        azureProperties.setFailFast(true);
        appProperties = new AppConfigProviderProperties();
        appProperties.setKeyVaultWaitTime(0);

        propertySource = new AzureConfigPropertySource(TEST_CONTEXT, operations, TEST_STORE_NAME, null,
                azureProperties, appProperties);
        
        testItems = new ArrayList<KeyValueItem>();
        testItems.add(item1);
        testItems.add(item2);
        testItems.add(item3);
    }

    @Test
    public void testPropCanBeInitAndQueried() {
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(featureSet);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() {
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        KeyValueItem slashedProp = createItem(TEST_CONTEXT, TEST_SLASH_KEY, TEST_SLASH_VALUE, null, EMPTY_CONTENT_TYPE);
        when(operations.getKeys(any(), any())).thenReturn(Arrays.asList(slashedProp)).thenReturn(FEATURE_ITEMS);

        FeatureSet featureSet = new FeatureSet();
        
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(1);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }

    @Test
    public void testFeatureFlagCanBeInitedAndQueried() {
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(FEATURE_ITEMS);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(featureSet);

        FeatureSet featureSetExpected = new FeatureSet();
        Feature feature = new Feature();
        feature.setId("Alpha");
        ArrayList<FeatureFilterEvaluationContext> filters = new ArrayList<FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext ffec = new FeatureFilterEvaluationContext();
        ffec.setName("TestFilter");
        filters.add(ffec);
        feature.setEnabledFor(filters);
        featureSetExpected.addFeature("Alpha", feature);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected, LinkedHashMap.class);

        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }

    @Test
    public void testFeatureFlagThrowError() throws IOException {
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(testItems)
        .thenReturn(FEATURE_ITEMS).thenReturn(FEATURE_ITEMS);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            assertEquals("Found Feature Flag /foo/test_key_1 with invalid Content Type of ", e.getMessage());
        }
    }
    
    @Test
    public void testFeatureFlagBuildError() {
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(FEATURE_ITEMS);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail();
        }
        propertySource.initFeatures(featureSet);

        FeatureSet featureSetExpected = new FeatureSet();
        Feature feature = new Feature();
        feature.setId("Alpha");
        ArrayList<FeatureFilterEvaluationContext> filters = new ArrayList<FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext ffec = new FeatureFilterEvaluationContext();
        ffec.setName("TestFilter");
        filters.add(ffec);
        feature.setEnabledFor(filters);
        featureSetExpected.addFeature("Alpha", feature);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected, LinkedHashMap.class);

        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }
    
    @Test
    public void testKeyVaultTest() throws Exception {
        testItems.add(keyVaultItem);
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        PowerMockito.whenNew(SecretClientBuilder.class).withNoArguments().thenReturn(secretClientBuilder);
        when(secretClientBuilder.endpoint(Mockito.anyString())).thenReturn(secretClientBuilder);
        when(secretClientBuilder.credential(Mockito.any())).thenReturn(secretClientBuilder);
        when(secretClientBuilder.buildAsyncClient()).thenReturn(secretAsyncClient);
        when(secretAsyncClient.getSecret(Mockito.any(Secret.class))).thenReturn(monoSecret);
        
        String secretValue = "secretValue";
        Secret secret = new Secret("mySecret", secretValue);
        when(monoSecret.block(Mockito.any())).thenReturn(secret);
        
        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(featureSet);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo(secretValue);
        verify(operations, times(2)).getKeys(any(), any());
    }
    
    @Test
    public void initNullValidContentTypeTest() {
        ArrayList<KeyValueItem> items = new ArrayList<KeyValueItem>();
        items.add(item3Null);
        when(operations.getKeys(any(), any())).thenReturn(items).thenReturn(new ArrayList<KeyValueItem>());
        
        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        
        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = items.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }
    
    @Test
    public void initNullInvalidContentTypeFeatureFlagTest() {
        ArrayList<KeyValueItem> items = new ArrayList<KeyValueItem>();
        items.add(featureItemNull);
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(items);
        
        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {

        }
        
        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = {};

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }
}
