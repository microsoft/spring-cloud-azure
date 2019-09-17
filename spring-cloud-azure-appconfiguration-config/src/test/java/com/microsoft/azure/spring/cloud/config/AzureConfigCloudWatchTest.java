/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyCache.class)
public class AzureConfigCloudWatchTest {
    @Mock
    private ConfigServiceOperations configOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PropertyCache propertyCache;

    @Mock
    private AzureCloudConfigProperties properties;
    
    private ArrayList<KeyValueItem> keys;

    @Mock
    private Map<String, List<String>> contextsMap;

    private AzureCloudConfigWatch watch;

    @Mock
    private Date date;

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
        keys = new ArrayList<KeyValueItem>();
        KeyValueItem kvi = new KeyValueItem();
        kvi.setKey("fake-etag/application/test.key");
        kvi.setValue("TestValue");
        keys.add(kvi);

        propertyCache = PropertyCache.getPropertyCache();
        KeyValueItem item = new KeyValueItem();
        item.setKey("fake-etag/application/test.key");
        item.setEtag("fake-etag");
        propertyCache.addToCache(item, TEST_STORE_NAME, new Date());

        watch = new AzureCloudConfigWatch(configOperations, properties, contextsMap, propertyCache);
    }

    @Test
    public void firstCallShouldPublishEvent() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        watch.setApplicationEventPublisher(eventPublisher);
        when(configOperations.getKeys(any(), any())).thenReturn(keys);

        List<KeyValueItem> mockResponse = initialResponse();

        when(configOperations.getRevisions(any(), any())).thenReturn(mockResponse);
        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        watch.setApplicationEventPublisher(eventPublisher);
        when(configOperations.getKeys(any(), any())).thenReturn(keys);
        when(configOperations.getRevisions(any(), any())).thenReturn(initialResponse()).thenReturn(updatedResponse());

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        verify(configOperations, times(1)).getRevisions(any(), any());

        watch.refreshConfigurations();

        // If there is a change it should update
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
        verify(configOperations, times(3)).getRevisions(any(), any());

        watch.refreshConfigurations();

        // If there is no change it shouldn't update
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
        verify(configOperations, times(4)).getRevisions(any(), any());
    }

    @Test
    public void nonUpdatedEtagsRemoved() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        watch.setApplicationEventPublisher(eventPublisher);
        when(configOperations.getKeys(any(), any())).thenReturn(keys);
        when(configOperations.getRevisions(any(), any())).thenReturn(initialResponse()).thenReturn(updatedResponse())
                .thenReturn(initialResponse());

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        verify(configOperations, times(1)).getRevisions(any(), any());

        watch.refreshConfigurations();

        // This time the main etag has been changed, but the one etag checked hasn't
        // changed
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        verify(configOperations, times(3)).getRevisions(any(), any());
    }
    
    @Test
    public void revisonsEndpointReturnsEmptyForKnownKey() throws Exception {
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
        watch.setApplicationEventPublisher(eventPublisher);
        when(configOperations.getKeys(any(), any())).thenReturn(keys);
        when(configOperations.getRevisions(any(), any())).thenReturn(initialResponse()).thenReturn(updatedResponse()).thenReturn(new ArrayList<KeyValueItem>());

        when(date.after(Mockito.any(Date.class))).thenReturn(true);
        watch.refreshConfigurations();
        watch.refreshConfigurations();

        // The first time an action happens it can update
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
        verify(configOperations, times(3)).getRevisions(any(), any());
    }

    private List<KeyValueItem> initialResponse() {
        KeyValueItem item = new KeyValueItem();
        item.setEtag("fake-etag");

        return Arrays.asList(item);
    }

    private List<KeyValueItem> updatedResponse() {
        KeyValueItem item = new KeyValueItem();
        item.setEtag("fake-etag-updated");

        return Arrays.asList(item);
    }

}
