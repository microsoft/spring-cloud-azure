/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.spring.cloud.context.core.Tuple;

import java.util.function.Function;

/**
 * @author Warren Zhu
 */
public interface EventHubClientFactory {

    /**
     * Return a function which accepts event hub name, then returns {@link EventHubClient}
     */
    Function<String, EventHubClient> getEventHubClientCreator();

    /**
     * Return a function which accepts {@link EventHubClient} and partition id, then returns {@link PartitionSender}
     */
    Function<Tuple<EventHubClient, String>, PartitionSender> getPartitionSenderCreator();

    /**
     * Return a function which accepts event hub name and consumer group, then returns {@link EventProcessorHost}
     */
    Function<Tuple<String, String>, EventProcessorHost> getProcessorHostCreator();

}
