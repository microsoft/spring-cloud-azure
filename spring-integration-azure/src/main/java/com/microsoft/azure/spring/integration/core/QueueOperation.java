/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

public interface QueueOperation<T> {

    boolean add(String destination, T t);

    T peek(String destination);

    T retrieve(String destination);

    boolean delete(String destination, T t);
}
