/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.api;

/**
 * Interface to provide the {@link Environment}
 *
 * @author Warren Zhu
 */
public interface EnvironmentProvider {
    Environment getEnvironment();
}
