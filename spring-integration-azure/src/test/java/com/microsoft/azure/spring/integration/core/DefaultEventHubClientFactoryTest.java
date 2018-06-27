/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.eventhub.DefaultEventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.EventHubClientFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventHubClient.class)
public class DefaultEventHubClientFactoryTest {

    @Mock
    private AzureAdmin azureAdmin;

    @Mock
    private EventHubNamespace eventHubNamespace;

    @Mock
    private EventHubClient eventHubClient;

    @Mock
    private PartitionSender partitionSender;

    @Mock
    private EventProcessorHost processorHost;

    @Mock
    private EventHub eventHub;

    private EventHubClientFactory factory;
    private String namespace = "namespace";
    private String eventHubName = "eventHub";
    private String consumerGroup = "group";
    private String partitionId = "1";

    @Before
    public void setUp() throws Exception {
        this.factory = new DefaultEventHubClientFactory(azureAdmin, namespace);
        when(azureAdmin.getOrCreateEventHubNamespace(eq(namespace))).thenReturn(eventHubNamespace);
        when(azureAdmin.getOrCreateEventHub(eq(namespace), eq(eventHubName))).thenReturn(eventHub);
        when(eventHubClient.getEventHubName()).thenReturn(eventHubName);
        when(partitionSender.getPartitionId()).thenReturn(partitionId);
        PowerMockito.mockStatic(EventHubClient.class);
        when(EventHubClient.createSync(isA(String.class), isA(Executor.class))).thenReturn(eventHubClient);
        when(eventHubClient.createPartitionSenderSync(eq(partitionId))).thenReturn(partitionSender);
        whenNew(EventProcessorHost.class).withAnyArguments().thenReturn(processorHost);
    }

    @Test
    @Ignore
    public void testGetClient() {
        EventHubClient client = factory.getOrCreateEventHubClient(eventHubName);

        assertEquals(eventHubName, client.getEventHubName());

        EventHubClient same = factory.getOrCreateEventHubClient(eventHubName);

        assertEquals(same, client);
    }

    @Test
    @Ignore
    public void testGetSender() {
        PartitionSender sender = factory.getOrCreatePartitionSender(eventHubName, partitionId);

        assertEquals(partitionId, sender.getPartitionId());

        PartitionSender same = factory.getOrCreatePartitionSender(eventHubName, partitionId);

        assertEquals(same, sender);
    }

    @Test
    @Ignore
    public void testGetEventProcessorHost() {
        EventProcessorHost host = factory.getOrCreateEventProcessorHost(eventHubName, consumerGroup);

        EventProcessorHost same = factory.getOrCreateEventProcessorHost(eventHubName, consumerGroup);

        assertEquals(host, same);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewDefaultPublisherFactory_nullProjectIdProvider() {
        new DefaultEventHubClientFactory(null,
                namespace);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewDefaultPublisherFactory_nullProjectId() {
        new DefaultEventHubClientFactory(azureAdmin, null);
    }
}
