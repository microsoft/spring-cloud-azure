/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

public interface QueueOperation<T> {

    boolean add(T t);

    T peek();

    T retrieve();

    boolean delete(T t);
}
