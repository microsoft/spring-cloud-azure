/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;

/**
 * @author Warren Zhu
 */
public interface EventHubClientFactory {

    EventHubClient getOrCreateEventHubClient(String eventHubName);

    PartitionSender getOrCreatePartitionSender(String eventHubName, String partitionId);

    EventProcessorHost getOrCreateEventProcessorHost(String eventHubName, String consumerGroup);
}
