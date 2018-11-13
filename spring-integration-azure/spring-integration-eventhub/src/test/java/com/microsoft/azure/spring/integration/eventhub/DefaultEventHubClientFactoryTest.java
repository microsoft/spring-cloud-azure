/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.api.Region;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageConnectionStringProvider;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EventHubClient.class, StorageConnectionStringProvider.class, EventProcessorHost.class})
public class DefaultEventHubClientFactoryTest {

    @Mock
    ResourceManagerProvider resourceManagerProvider;

    @Mock
    StorageAccountManager storageAccountManager;

    @Mock
    EventHubClient eventHubClient;

    @Mock
    PartitionSender partitionSender;

    @Mock
    EventProcessorHost eventProcessorHost;

    @Mock
    StorageAccount storageAccount;

    private EventHubClientFactory clientFactory;
    private String checkpointStorageAccount = "sa";
    private String eventHubName = "eventHub";
    private String consumerGroup = "group";
    private String connectionString = "conStr";
    private String partitionId = "1";

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(EventHubClient.class);
        when(EventHubClient.createSync(eq(connectionString), any())).thenReturn(eventHubClient);
        when(eventHubClient.createPartitionSenderSync(eq(partitionId))).thenReturn(partitionSender);

        PowerMockito.mockStatic(StorageConnectionStringProvider.class);
        when(StorageConnectionStringProvider.getConnectionString(isA(StorageAccount.class), isA(Region.class)))
                .thenReturn(connectionString);
        when(resourceManagerProvider.getStorageAccountManager()).thenReturn(storageAccountManager);
        when(storageAccountManager.getOrCreate(any())).thenReturn(storageAccount);
        PowerMockito.whenNew(EventProcessorHost.class).withAnyArguments().thenReturn(eventProcessorHost);
        this.clientFactory = new DefaultEventHubClientFactory(checkpointStorageAccount,
                (s) -> connectionString);
    }

    @Test
    public void testGetEventHubClient() {
        EventHubClient client = clientFactory.getOrCreateClient(eventHubName);
        assertNotNull(client);
        EventHubClient another = clientFactory.getOrCreateClient(eventHubName);
        assertEquals(client, another);
    }

    @Test
    public void testGetPartitionSender() {
        PartitionSender sender = clientFactory.getOrCreatePartitionSender(this.eventHubName, partitionId);
        assertNotNull(sender);
        PartitionSender another = clientFactory.getOrCreatePartitionSender(eventHubName, partitionId);
        assertEquals(sender, another);
    }

    @Test
    @Ignore("Cannot mock EventProcessorHost constructor")
    public void testGetEventProcessorHost() {
        EventProcessorHost host = clientFactory.getOrCreateEventProcessorHost(eventHubName, consumerGroup);
        assertNotNull(host);
        EventProcessorHost another = clientFactory.getOrCreateEventProcessorHost(eventHubName, consumerGroup);
        assertEquals(host, another);
    }
}
