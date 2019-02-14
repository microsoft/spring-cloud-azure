/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AzureConfigCloudWatchTest {
    @Mock
    private ConfigServiceOperations configOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AzureCloudConfigProperties properties = new AzureCloudConfigProperties();

    private AzureCloudConfigWatch watch;

    private TaskScheduler scheduler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        scheduler = new ThreadPoolTaskScheduler();
        ((ThreadPoolTaskScheduler) scheduler).initialize();

        ConfigStore store = new ConfigStore();
        store.setName(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);
        store.setWatchedKey(TEST_WATCH_KEY);
        properties.setStores(Arrays.asList(store));

        properties.getWatch().setDelay(Duration.ofSeconds(1));

        Map<String, List<String>> contextsMap = new ConcurrentHashMap<>();
        contextsMap.put(TEST_STORE_NAME, Arrays.asList(TEST_ETAG));
        watch = new AzureCloudConfigWatch(configOperations, properties, scheduler, contextsMap);
        watch.setApplicationEventPublisher(eventPublisher);
    }

    @After
    public void tearDown() {
        watch.stop();
    }

    @Test
    public void firstCallShouldNotPublishEvent() {
        List<KeyValueItem> mockResponse = initialResponse();
        when(configOperations.getRevisions(any(), any())).thenReturn(mockResponse);
        watch.start();
        watch.watchConfigKeyValues();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws InterruptedException {
        List<KeyValueItem> mockResponse = initialResponse();
        when(configOperations.getRevisions(any(), any())).thenReturn(mockResponse);
        watch.start();
        watch.watchConfigKeyValues();

        List<KeyValueItem> updatedResponse = updatedResponse();
        when(configOperations.getRevisions(any(), any())).thenReturn(updatedResponse);
        Thread.sleep(properties.getWatch().getDelay().getSeconds() * 1000 * 2);
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
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
