/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

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
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";
    private static final String KEY_VAULT_CONTENT_TYPE = 
            "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    private static final AzureCloudConfigProperties TEST_PROPS = new AzureCloudConfigProperties();

    public static List<KeyValueItem> testItems = new ArrayList<>();

    public static final List<KeyValueItem> FEATURE_ITEMS = new ArrayList<>();

    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
            EMPTY_CONTENT_TYPE);

    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2,
            EMPTY_CONTENT_TYPE);

    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
            EMPTY_CONTENT_TYPE);

    private static final KeyValueItem featureItem = createItem(".appconfig.featureflag/", "Alpha", FEATURE_VALUE,
            FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final KeyValueItem keyVaultItem = createItem(TEST_CONTEXT, TEST_KEY_VAULT_1, TEST_VALUE_VAULT_1,
            TEST_LABEL_VAULT_1, KEY_VAULT_CONTENT_TYPE);

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private AzureConfigPropertySource propertySource;

    private static ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ConfigServiceOperations operations;

    private PropertyCache propertyCache;
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
        propertyCache = PropertyCache.resetPropertyCache();
        
        testItems = new ArrayList<KeyValueItem>();
        testItems.add(item1);
        testItems.add(item2);
        testItems.add(item3);
    }

    @Test
    public void testPropCanBeInitAndQueried() {
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

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

        try {
            propertySource.initProperties(propertyCache);
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

        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

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

    @Test
    public void testFeatureFlagThrowError() throws IOException {
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(testItems)
        .thenReturn(FEATURE_ITEMS).thenReturn(FEATURE_ITEMS);

        propertyCache = PropertyCache.resetPropertyCache();

        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            assertEquals("Found Feature Flag /foo/test_key_1 with invalid Content Type of ", e.getMessage());
        }
    }

    @Test
    public void testFeatureFlagBuildError() {
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(FEATURE_ITEMS);

        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            fail();
        }
        propertySource.initFeatures(propertyCache);

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

    @Test
    public void testWatchUpdateConfigurations() throws ParseException {
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        Duration delay = Duration.ofSeconds(0);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse("20190202");
        Date testDate = new Date(date.getTime() - 2);
        propertyCache.addKeyValuesToCache(testItems, TEST_STORE_NAME, testDate);
        propertyCache.addToCache(featureItem, TEST_STORE_NAME, testDate);

        propertyCache.findNonCachedKeys(delay, TEST_STORE_NAME);
        propertyCache.addContext(TEST_STORE_NAME, TEST_CONTEXT);
        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        verify(operations, times(4)).getKeys(any(), any());
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
        
        Duration delay = Duration.ofSeconds(0);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse("20190202");
        Date testDate = new Date(date.getTime() - 2);
        propertyCache.addKeyValuesToCache(testItems, TEST_STORE_NAME, testDate);
        propertyCache.addToCache(featureItem, TEST_STORE_NAME, testDate);

        propertyCache.findNonCachedKeys(delay, TEST_STORE_NAME);
        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

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
    public void testKeyVaultReloadTest() throws Exception {
        testItems.add(keyVaultItem);
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        when(operations.getKeys(any(), any())).thenReturn(testItems).thenReturn(FEATURE_ITEMS);
        PowerMockito.whenNew(SecretClientBuilder.class).withNoArguments().thenReturn(secretClientBuilder);
        when(secretClientBuilder.endpoint(Mockito.anyString())).thenReturn(secretClientBuilder);
        when(secretClientBuilder.credential(Mockito.any())).thenReturn(secretClientBuilder);
        when(secretClientBuilder.buildAsyncClient()).thenReturn(secretAsyncClient);
        when(secretAsyncClient.getSecret(Mockito.any(Secret.class))).thenReturn(monoSecret);
        
        String secretValue = "secretValue";
        Secret secret = new Secret("mySecret", secretValue);
        when(monoSecret.block(Mockito.any())).thenReturn(secret);
        Duration delay = Duration.ofSeconds(0);
        propertyCache.addContext(TEST_STORE_NAME, TEST_CONTEXT);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse("20190202");
        Date testDate = new Date(date.getTime() - 2);
        propertyCache.addKeyValuesToCache(testItems, TEST_STORE_NAME, testDate);
        propertyCache.addToCache(featureItem, TEST_STORE_NAME, testDate);

        propertyCache.findNonCachedKeys(delay, TEST_STORE_NAME);
        try {
            propertySource.initProperties(propertyCache);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo(secretValue);
        verify(operations, times(5)).getKeys(any(), any());
    }
    
    @Test
    public void awaitOnError() {
        expected.expect(NullPointerException.class);
        
        KeyValueItem badFeature = new KeyValueItem();
        badFeature.setContentType(FEATURE_FLAG_CONTENT_TYPE);
        badFeature.setKey(".appconfig.featureflag/");
        propertyCache.addToCache(badFeature, "test", new Date());
        propertySource.initFeatures(propertyCache);
    }

    @Test
    public void postProcessConfigsTest() {
        AbstractEnvironment envMock = Mockito.mock(AbstractEnvironment.class);
        OriginTrackedMapPropertySource bootstrapSourceMock = Mockito.mock(OriginTrackedMapPropertySource.class);
        MutablePropertySources sources = new MutablePropertySources();

        HashMap<String, Object> source = new HashMap<String, Object>();
        HashMap<String, Object> primaryConfigs = new HashMap<String, Object>();
        HashMap<String, Object> secondaryConfigs = new HashMap<String, Object>();

        source.put("KEY_1", OriginTrackedValue.of("${PLACE_1}"));
        source.put("KEY_2", OriginTrackedValue.of("${PLACE_2}"));
        source.put("KEY_3", OriginTrackedValue.of("${PLACE_3}"));
        source.put("KEY_4", OriginTrackedValue.of("${PLACE_1}-${PLACE_3}"));

        primaryConfigs.put("PLACE_1", "P1_1");
        primaryConfigs.put("PLACE_2", "P1_2");

        secondaryConfigs.put("PLACE_2", "P2_2");
        secondaryConfigs.put("PLACE_3", "P2_3");

        MapPropertySource primarySource = new MapPropertySource("primary", primaryConfigs);
        MapPropertySource secondarySource = new MapPropertySource("secondary", secondaryConfigs);

        sources.addFirst(secondarySource);
        sources.addFirst(primarySource);

        when(bootstrapSourceMock.getSource()).thenReturn(source);
        when(envMock.resolvePlaceholders(Mockito.matches("P1_1"))).thenReturn("P1_1");
        when(envMock.resolvePlaceholders(Mockito.matches("P1_2"))).thenReturn("P1_2");
        when(envMock.resolvePlaceholders(Mockito.matches("P2_3"))).thenReturn("P2_3");
        when(envMock.resolvePlaceholders(Mockito.matches("P1_1-P2_3"))).thenReturn("P1_1-P2_3");

        propertySource.postProcessConfigurations(envMock, bootstrapSourceMock, sources);
        assertEquals("P1_1", propertySource.getProperty("KEY_1"));
        assertEquals("P1_2", propertySource.getProperty("KEY_2"));
        assertEquals("P2_3", propertySource.getProperty("KEY_3"));
        assertEquals("P1_1-P2_3", propertySource.getProperty("KEY_4"));
    }
}
