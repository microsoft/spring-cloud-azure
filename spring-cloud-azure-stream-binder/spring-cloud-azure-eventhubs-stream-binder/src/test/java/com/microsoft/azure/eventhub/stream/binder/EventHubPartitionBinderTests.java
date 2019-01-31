/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.servicebus.stream.binder.test.AzurePartitionBinderTests;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.support.EventHubTestOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

/**
 * Test cases are defined in super class
 *
 * @author Warren Zhu
 */
@RunWith(MockitoJUnitRunner.class)
public class EventHubPartitionBinderTests extends
        AzurePartitionBinderTests<EventHubTestBinder, ExtendedConsumerProperties<EventHubConsumerProperties>,
                ExtendedProducerProperties<EventHubProducerProperties>> {

    @Mock
    EventHubClientFactory clientFactory;

    @Mock
    PartitionContext context;

    private EventHubTestBinder binder;

    @Before
    public void setUp() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.context.getPartitionId()).thenReturn("1");
        when(this.context.checkpoint()).thenReturn(future);
        this.binder = new EventHubTestBinder(new EventHubTestOperation(clientFactory, () -> context));
    }

    @Override
    protected String getClassUnderTestName() {
        return EventHubTestBinder.class.getSimpleName();
    }

    @Override
    protected EventHubTestBinder getBinder() throws Exception {
        return this.binder;
    }

    @Override
    protected ExtendedConsumerProperties<EventHubConsumerProperties> createConsumerProperties() {
        ExtendedConsumerProperties<EventHubConsumerProperties> properties =
                new ExtendedConsumerProperties<>(new EventHubConsumerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        properties.getExtension().setStartPosition(StartPosition.EARLIEST);
        return properties;
    }

    @Override
    protected ExtendedProducerProperties<EventHubProducerProperties> createProducerProperties() {
        ExtendedProducerProperties<EventHubProducerProperties> properties =
                new ExtendedProducerProperties<>(new EventHubProducerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }
}
