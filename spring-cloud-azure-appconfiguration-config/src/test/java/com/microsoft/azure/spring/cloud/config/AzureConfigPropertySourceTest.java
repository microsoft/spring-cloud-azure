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
import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_FLAG_CONTENT_TYPE;
import static com.microsoft.azure.spring.cloud.config.Constants.KEY_VAULT_CONTENT_TYPE;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.Feature;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureFilterEvaluationContext;
import com.microsoft.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RunWith(PowerMockRunner.class)
@PrepareForTest({AzureConfigPropertySource.class})
public class AzureConfigPropertySourceTest {
    private static final String EMPTY_CONTENT_TYPE = "";
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";
    private static final String KEY_VAULT_CONTENT_TYPE = 
            "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";
    
    private static final AzureCloudConfigProperties TEST_PROPS = new AzureCloudConfigProperties();

    public static final List<KeyValueItem> TEST_ITEMS = new ArrayList<>();

    public static final List<KeyValueItem> FEATURE_ITEMS = new ArrayList<>();

    public static List<ConfigurationSetting> testItems = new ArrayList<>();

    public static final List<ConfigurationSetting> FEATURE_ITEMS = new ArrayList<>();

    private static final ConfigurationSetting item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1);

    private static final ConfigurationSetting item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2);

    private static final ConfigurationSetting item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3);

    private static final ConfigurationSetting featureItem = createItem(".appconfig.featureflag/", "Alpha",
            FEATURE_VALUE,
            FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final ConfigurationSetting keyVaultItem = createItem(TEST_CONTEXT, TEST_KEY_VAULT_1,
            TEST_VALUE_VAULT_1,
            TEST_LABEL_VAULT_1);
    public List<KeyValueItem> testItems = new ArrayList<>();

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private AzureConfigPropertySource propertySource;

    private static ObjectMapper mapper = new ObjectMapper();

    private HashMap<String, KeyVaultClient> keyVaultClients;

    @Mock
    private KeyVaultClient keyVaultClient;

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

    @Mock
    private SecretBundle secretBundleMock;

    PropertyCache propertyCache;

    @Mock
    private ClientStore clientStoreMock;

    @Mock
    private ConfigurationAsyncClient configClientMock;

    @Mock
    private PagedFlux<ConfigurationSetting> settingsMock;

    @Mock
    private Flux<PagedResponse<ConfigurationSetting>> pageMock;

    @Mock
    private Mono<List<PagedResponse<ConfigurationSetting>>> collectionMock;

    @Mock
    private List<PagedResponse<ConfigurationSetting>> itemsMock;

    @Mock
    private Iterator<PagedResponse<ConfigurationSetting>> itemsIteratorMock;

    @Mock
    private PagedResponse<ConfigurationSetting> pagedResponseMock;

    @Mock
    private Iterator<ConfigurationSetting> configurationsIterator;

    @BeforeClass
    public static void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING);

        keyVaultItem.contentType(KEY_VAULT_CONTENT_TYPE);
        featureItem.contentType(FEATURE_FLAG_CONTENT_TYPE);
        FEATURE_ITEMS.add(featureItem);
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        azureProperties = new AzureCloudConfigProperties();
        azureProperties.setFailFast(true);
        appProperties = new AppConfigProviderProperties();
        appProperties.setKeyVaultWaitTime(0);
        propertySource = new AzureConfigPropertySource(TEST_CONTEXT, TEST_STORE_NAME, "\0",
                new AzureCloudConfigProperties());

        testItems = new ArrayList<ConfigurationSetting>();
        testItems.add(item1);
        testItems.add(item2);
        testItems.add(item3);

        keyVaultClients = new HashMap<String, KeyVaultClient>();
        keyVaultClients.put("test.key.vault.com", keyVaultClient);

        when(configClientMock.listSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.byPage()).thenReturn(pageMock);
        when(pageMock.collectList()).thenReturn(collectionMock);
        when(collectionMock.block()).thenReturn(itemsMock);
        when(itemsMock.iterator()).thenReturn(itemsIteratorMock);
        when(itemsIteratorMock.next()).thenReturn(pagedResponseMock);
    }

    @Test
    public void testPropCanBeInitAndQueried() {
        propertyCache = PropertyCache.resetPropertyCache();

        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(testItems)
                .thenReturn(FEATURE_ITEMS);

        try {
            propertySource.initProperties(propertyCache, clientStoreMock);
        } catch (IOException | URISyntaxException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.key().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() {
        propertyCache = PropertyCache.resetPropertyCache();
        ConfigurationSetting slashedProp = createItem(TEST_CONTEXT, TEST_SLASH_KEY, TEST_SLASH_VALUE, null);
        List<ConfigurationSetting> settings = new ArrayList<ConfigurationSetting>();
        settings.add(slashedProp);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(settings)
                .thenReturn(new ArrayList<ConfigurationSetting>());

        try {
            propertySource.initProperties(propertyCache, clientStoreMock);
        } catch (IOException | URISyntaxException e) {
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
        propertyCache = PropertyCache.resetPropertyCache();
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString()))
                .thenReturn(new ArrayList<ConfigurationSetting>()).thenReturn(FEATURE_ITEMS);


        try {
            propertySource.initProperties(propertyCache, clientStoreMock);
        } catch (IOException | URISyntaxException e) {
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
        when(operations.getKeys(any(), any())).thenReturn(new ArrayList<KeyValueItem>()).thenReturn(TEST_ITEMS)
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
        Secret secret = new Secret("mySecret", "mySecret");
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
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecret");
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
        
        Secret secret = new Secret("mySecret", "mySecret");
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
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecret");
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

    @Test
    public void testWatchUpdateConfigurations() throws ParseException {
        propertyCache = PropertyCache.resetPropertyCache();
        Duration delay = Duration.ofSeconds(0);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse("20190202");
        Date testDate = new Date(date.getTime() - 2);
        propertyCache.addKeyValuesToCache(testItems, TEST_STORE_NAME, testDate);
        propertyCache.addToCache(featureItem, TEST_STORE_NAME, testDate);

        propertyCache.findNonCachedKeys(delay, TEST_STORE_NAME);
        propertyCache.addContext(TEST_STORE_NAME, TEST_CONTEXT);

        when(clientStoreMock.getConfigurationClient(Mockito.anyString())).thenReturn(configClientMock);

        try {
            propertySource.initProperties(propertyCache, clientStoreMock);
        } catch (IOException | URISyntaxException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.key().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testKeyVaultTest() throws ParseException {
        propertyCache = PropertyCache.resetPropertyCache();
        testItems.add(keyVaultItem);

        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(testItems)
                .thenReturn(new ArrayList<ConfigurationSetting>());
        when(clientStoreMock.getKeyVaultClient(Mockito.anyString())).thenReturn(keyVaultClient);
        when(keyVaultClient.getSecret(Mockito.any())).thenReturn(secretBundleMock);
        when(secretBundleMock.value()).thenReturn("mySecret");
        Duration delay = Duration.ofSeconds(0);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse("20190202");
        Date testDate = new Date(date.getTime() - 2);
        propertyCache.addKeyValuesToCache(testItems, TEST_STORE_NAME, testDate);
        propertyCache.addToCache(featureItem, TEST_STORE_NAME, testDate);

        propertyCache.findNonCachedKeys(delay, TEST_STORE_NAME);
        try {
            propertySource.initProperties(propertyCache, clientStoreMock);
        } catch (IOException | URISyntaxException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.key().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecret");
    }

    @Test
    public void testKeyVaultReloadTest() throws ParseException {
        propertyCache = PropertyCache.resetPropertyCache();
        testItems.add(keyVaultItem);

        when(clientStoreMock.getConfigurationClient(Mockito.anyString())).thenReturn(configClientMock);
        when(clientStoreMock.getKeyVaultClient(Mockito.anyString())).thenReturn(keyVaultClient);
        when(keyVaultClient.getSecret(Mockito.any())).thenReturn(secretBundleMock);
        when(secretBundleMock.value()).thenReturn("mySecret");
        Duration delay = Duration.ofSeconds(0);
        propertyCache.addContext(TEST_STORE_NAME, TEST_CONTEXT);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse("20190202");
        Date testDate = new Date(date.getTime() - 2);
        propertyCache.addKeyValuesToCache(testItems, TEST_STORE_NAME, testDate);
        propertyCache.addToCache(featureItem, TEST_STORE_NAME, testDate);

        propertyCache.findNonCachedKeys(delay, TEST_STORE_NAME);
        try {
            propertySource.initProperties(propertyCache, clientStoreMock);
        } catch (IOException | URISyntaxException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(propertyCache);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
                .map(t -> t.key().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecret");
    }
    }
}
