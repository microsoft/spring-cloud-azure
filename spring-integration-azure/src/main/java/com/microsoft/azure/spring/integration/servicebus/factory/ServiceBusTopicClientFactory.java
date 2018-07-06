/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.servicebus.ISubscriptionClient;
import com.microsoft.azure.spring.cloud.context.core.Tuple;

import java.util.function.Function;

/**
 * Factory to return functional creator of service bus topic and subscription client
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicClientFactory extends ServiceBusSenderFactory {
    /**
     * Return a function which accepts service bus topic and subscription name, then returns {@link ISubscriptionClient}
     */
    Function<Tuple<String, String>, ISubscriptionClient> getSubscriptionClientCreator();
}
