/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.spring.cloud.context.core.api.Environment;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import org.springframework.lang.NonNull;

/**
 * A {@link com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider} implementation that based on
 * {@link com.microsoft.azure.spring.cloud.context.core.config.AzureProperties}.
 *
 * @author Warren Zhu
 */
public class DefaultEnvironmentProvider implements EnvironmentProvider {

    private Environment environment = Environment.GLOBAL;

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
