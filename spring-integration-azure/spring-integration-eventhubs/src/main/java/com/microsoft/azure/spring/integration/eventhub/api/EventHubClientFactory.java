/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.api;

import java.util.Optional;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;

/**
 * @author Warren Zhu
 * @author Domenico Sibilio
 */
public interface EventHubClientFactory {

    EventHubClient getOrCreateClient(String name);

    PartitionSender getOrCreatePartitionSender(String eventhub, String partition);

    EventProcessorHost getOrCreateEventProcessorHost(String name, String consumerGroup);

    Optional<EventProcessorHost> getEventProcessorHost(String name, String consumerGroup);

    EventProcessorHost removeEventProcessorHost(String name, String consumerGroup);

}
