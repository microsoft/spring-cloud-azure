/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ETAG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyCache.class)
public class AzureConfigCloudWatchTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    PropertyCache propertyCache;

    @Mock
    private AzureCloudConfigProperties properties;

    @Mock
    Map<String, List<String>> contextsMap;

    AzureCloudConfigWatch watch;

    ArrayList<ConfigurationSetting> keys;

    @Mock
    Date date;

    @Mock
    private ClientStore clientStoreMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ConfigStore store = new ConfigStore();
        store.setName(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);
        store.setWatchedKey("/application/*");
        properties = new AzureCloudConfigProperties();
        properties.setStores(Arrays.asList(store));

        properties.getWatch().setDelay(Duration.ofSeconds(1));

        contextsMap = new ConcurrentHashMap<>();
        contextsMap.put(TEST_STORE_NAME, Arrays.asList(TEST_ETAG));
        keys = new ArrayList<ConfigurationSetting>();
        ConfigurationSetting kvi = new ConfigurationSetting();
        kvi.key("fake-etag/application/test.key");
        kvi.value("TestValue");
        keys.add(kvi);

        propertyCache = PropertyCache.resetPropertyCache();
        ConfigurationSetting item = new ConfigurationSetting();
        item.key("fake-etag/application/test.key");
        item.etag("fake-etag");
        propertyCache.addToCache(item, TEST_STORE_NAME, new Date());

        watch = new AzureCloudConfigWatch(properties, contextsMap, propertyCache, clientStoreMock);
    }

    @Test
    public void firstCallShouldPublishEvent() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        watch.setApplicationEventPublisher(eventPublisher);

        when(clientStoreMock.listSettingRevisons(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        when(clientStoreMock.listSettingRevisons(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse())
        .thenReturn(updatedResponse());
        watch.setApplicationEventPublisher(eventPublisher);

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        watch.refreshConfigurations();

        // If there is a change it should update
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watch.refreshConfigurations();

        // If there is no change it shouldn't update
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void nonUpdatedEtagsRemoved() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);

        when(clientStoreMock.listSettingRevisons(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse())
                .thenReturn(updatedResponse()).thenReturn(initialResponse());

        watch.setApplicationEventPublisher(eventPublisher);

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        watch.refreshConfigurations();

        // This time the main etag has been changed, but the one etag checked hasn't
        // changed
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    private List<ConfigurationSetting> initialResponse() {
        ConfigurationSetting item = new ConfigurationSetting();
        item.etag("fake-etag");

        return Arrays.asList(item);
    }

    private List<ConfigurationSetting> updatedResponse() {
        ConfigurationSetting item = new ConfigurationSetting();
        item.etag("fake-etag-updated");

        return Arrays.asList(item);
    }

}
