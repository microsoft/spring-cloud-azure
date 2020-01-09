/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public final class StateHolder {

    private StateHolder() {
        throw new IllegalStateException("Should not be callable.");
    }

    private static ConcurrentHashMap<String, ConfigurationSetting> state = 
            new ConcurrentHashMap<String, ConfigurationSetting>();

    public static ConfigurationSetting getState(String name) {
        return state.get(name);
    }

    public static void setState(String name, ConfigurationSetting setting) {
        state.put(name, setting);
    }

}
