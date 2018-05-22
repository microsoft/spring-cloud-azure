/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.core;

import com.microsoft.azure.eventhubs.EventData;

import java.util.concurrent.CompletableFuture;

/**
 * Azure event hub operation to support send data synchronously or asynchronously
 *
 * @author Warren Zhu
 */
public interface EventHubOperation {

    void send(String eventHubName, EventData eventData, PartitionSupplier partitionSupplier);

    CompletableFuture sendAsync(String eventHubName, EventData eventData, PartitionSupplier partitionSupplier);

}
