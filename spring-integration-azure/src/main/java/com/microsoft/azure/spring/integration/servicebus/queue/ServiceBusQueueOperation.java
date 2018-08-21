/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.spring.integration.core.SubscribeOperation;

import java.util.UUID;

/**
 * Azure service bus queue operation to support send {@link IMessage} asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface ServiceBusQueueOperation extends SendOperation, SubscribeOperation<IMessage, UUID> {
}
