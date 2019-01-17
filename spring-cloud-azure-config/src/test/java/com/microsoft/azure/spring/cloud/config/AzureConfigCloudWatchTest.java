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

import java.util.Arrays;
import java.util.List;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
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
        TestUtils.addStore(properties, TEST_STORE_NAME, TEST_CONN_STRING);
        watch = new AzureCloudConfigWatch(configOperations, properties, scheduler);
        watch.setApplicationEventPublisher(eventPublisher);
    }

    @After
    public void tearDown() {
        watch.stop();
    }

    @Test
    public void firstCallShouldNotPublishEvent() {
        List<KeyValueItem> mockResponse = initialResponse();
        when(configOperations.getKeys(any(), any(), any())).thenReturn(mockResponse);
        watch.start();
        watch.watchConfigKeyValues();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws InterruptedException {
        List<KeyValueItem> mockResponse = initialResponse();
        when(configOperations.getKeys(any(), any(), any())).thenReturn(mockResponse);
        watch.start();
        watch.watchConfigKeyValues();

        List<KeyValueItem> updatedResponse = updatedResponse();
        when(configOperations.getKeys(any(), any())).thenReturn(updatedResponse);
        Thread.sleep(properties.getWatch().getDelay().getSeconds() * 1000 * 2);
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    private List<KeyValueItem> initialResponse() {
        KeyValueItem item = new KeyValueItem();
        item.setKey("fake-key");
        item.setValue("fake-value");
        item.setEtag("fake-etag");

        return Arrays.asList(item);
    }

    private List<KeyValueItem> updatedResponse() {
        KeyValueItem item = new KeyValueItem();
        item.setKey("fake-key");
        item.setValue("fake-value-updated");
        item.setEtag("fake-etag-updated");

        return Arrays.asList(item);
    }

}
