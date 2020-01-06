/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.Constants.CONFIGURATION_SUFFIX;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ETAG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties.AutoRefresh;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

@RunWith(PowerMockRunner.class)
public class AzureConfigCloudWatchTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AzureCloudConfigProperties properties;

    private ArrayList<ConfigurationSetting> keys;

    @Mock
    private Map<String, List<String>> contextsMap;

    @Mock
    private AutoRefresh refresh;

    AzureCloudConfigRefresh configRefresh;

    @Mock
    private Date date;

    @Mock
    private ClientStore clientStoreMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);
        store.setWatchedKey("/application/*");
        
        properties = new AzureCloudConfigProperties();
        properties.setStores(Arrays.asList(store));

        properties.getAutoRefresh().setInterval(Duration.ofSeconds(-60));

        contextsMap = new ConcurrentHashMap<>();
        contextsMap.put(TEST_STORE_NAME, Arrays.asList(TEST_ETAG));
        keys = new ArrayList<ConfigurationSetting>();
        ConfigurationSetting kvi = new ConfigurationSetting();
        kvi.setKey("fake-etag/application/test.key");
        kvi.setValue("TestValue");
        keys.add(kvi);

        ConfigurationSetting item = new ConfigurationSetting();
        item.setKey("fake-etag/application/test.key");
        item.setETag("fake-etag");
        
        configRefresh = new AzureCloudConfigRefresh(properties, contextsMap, clientStoreMock);
    }

    @Test
    public void firstCallShouldPublishEvent() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        when(clientStoreMock.listSettingRevisons(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        configRefresh.refreshConfigurations();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        when(clientStoreMock.listSettingRevisons(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse())
                .thenReturn(updatedResponse());
        configRefresh.setApplicationEventPublisher(eventPublisher);

        when(date.after(Mockito.any(Date.class))).thenReturn(true);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        StateHolder.setState(TEST_STORE_NAME + CONFIGURATION_SUFFIX, new ConfigurationSetting());

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated");

        StateHolder.setState(TEST_STORE_NAME + CONFIGURATION_SUFFIX, updated);

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void notRefreshTime() throws Exception {
        properties.getAutoRefresh().setInterval(Duration.ofSeconds(60));
        AzureCloudConfigRefresh watchLargeDelay = new AzureCloudConfigRefresh(properties, contextsMap, clientStoreMock);
        
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        watchLargeDelay.setApplicationEventPublisher(eventPublisher);

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watchLargeDelay.refreshConfigurations();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    private List<ConfigurationSetting> initialResponse() {
        ConfigurationSetting item = new ConfigurationSetting();
        item.setETag("fake-etag");

        return Arrays.asList(item);
    }

    private List<ConfigurationSetting> updatedResponse() {
        ConfigurationSetting item = new ConfigurationSetting();
        item.setETag("fake-etag-updated");

        return Arrays.asList(item);
    }

}
