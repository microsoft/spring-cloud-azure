/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;

final class StateHolder {

    private StateHolder() {
        throw new IllegalStateException("Should not be callable.");
    }

    private static ConcurrentHashMap<String, State> state = new ConcurrentHashMap<String, State>();

    private static ConcurrentHashMap<String, Boolean> loadState = new ConcurrentHashMap<String, Boolean>();

    /**
     * @return the state
     */
    static State getState(String endpoint, AppConfigurationStoreTrigger trigger) {
        return state.get(endpoint + trigger.toString());
    }

    /**
     * @param state the etagState to set
     */
    static void setState(String endpoint, AppConfigurationStoreTrigger trigger, ConfigurationSetting config) {
        state.put(endpoint + trigger.toString(), new State(config));
    }
    
    static void resetState(String endpoint, AppConfigurationStoreTrigger trigger) {
        String key = endpoint + trigger.toString();
        state.put(key, state.get(key));
    }

    /**
     * @return the loadState
     */
    static Boolean getLoadState(String name) {
        Boolean loadstate = loadState.get(name);
        return loadstate == null ? false : loadstate;
    }

    /**
     * @param loadState the loadState to set
     */
    static void setLoadState(String name, Boolean loaded) {
        loadState.put(name, loaded);
    }
}
