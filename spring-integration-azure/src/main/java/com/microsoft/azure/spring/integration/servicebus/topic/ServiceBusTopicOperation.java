/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.spring.integration.core.SendOperation;
import com.microsoft.azure.spring.integration.core.SubscribeByGroupOperation;

import java.util.UUID;

/**
 * Azure service bus topic operation to support send {@link IMessage} asynchronously
 * and subscribe by {@link ServiceBusSubscription} as consumer group
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicOperation extends SendOperation<IMessage>, SubscribeByGroupOperation<IMessage, UUID> {
}
