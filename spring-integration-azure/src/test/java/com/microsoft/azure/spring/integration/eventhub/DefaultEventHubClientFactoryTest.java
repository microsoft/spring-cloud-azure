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
import com.microsoft.azure.spring.cloud.context.core.impl.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EventHubClient.class, StorageConnectionStringProvider.class, EventProcessorHost.class})
public class DefaultEventHubClientFactoryTest {

    @Mock
    AzureAdmin azureAdmin;

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
        when(StorageConnectionStringProvider.getConnectionString(isA(StorageAccount.class))).thenReturn(
                connectionString);
        when(azureAdmin.getOrCreateStorageAccount(any())).thenReturn(storageAccount);
        PowerMockito.whenNew(EventProcessorHost.class).withAnyArguments().thenReturn(eventProcessorHost);
        this.clientFactory =
                new DefaultEventHubClientFactory(azureAdmin, checkpointStorageAccount, (s) -> connectionString);
    }

    @Test
    public void testGetEventHubClient() {
        EventHubClient client = clientFactory.getEventHubClientCreator().apply(eventHubName);
        assertNotNull(client);
        EventHubClient another = clientFactory.getEventHubClientCreator().apply(eventHubName);
        assertEquals(client, another);
    }

    @Test
    public void testGetPartitionSender() {
        EventHubClient client = clientFactory.getEventHubClientCreator().apply(eventHubName);
        PartitionSender sender = clientFactory.getPartitionSenderCreator().apply(Tuple.of(client, partitionId));
        assertNotNull(sender);
        PartitionSender another = clientFactory.getPartitionSenderCreator().apply(Tuple.of(client, partitionId));
        assertEquals(sender, another);
    }

    @Test
    @Ignore("Cannot mock EventProcessorHost constructor")
    public void testGetEventProcessorHost() {
        EventProcessorHost host = clientFactory.getProcessorHostCreator().apply(Tuple.of(eventHubName, consumerGroup));
        assertNotNull(host);
        EventProcessorHost another =
                clientFactory.getProcessorHostCreator().apply(Tuple.of(eventHubName, consumerGroup));
        assertEquals(host, another);
    }
}
