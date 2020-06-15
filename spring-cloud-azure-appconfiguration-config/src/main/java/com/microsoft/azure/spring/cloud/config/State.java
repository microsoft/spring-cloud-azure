/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;

class State {
    private final ConfigurationSetting configurationSetting;

    private final Date notCachedTime;

    State(ConfigurationSetting configurationSetting, AppConfigurationStoreMonitoring monitoring) {
        this.configurationSetting = configurationSetting;
        notCachedTime = DateUtils.addSeconds(new Date(), Math.toIntExact(monitoring.getCacheExpiration().getSeconds()));
    }

    /**
     * Creates a new State object that is already expired.
     * 
     * @param oldState
     */
    State(State oldState) {
        this.configurationSetting = oldState.getConfigurationSetting();
        this.notCachedTime = DateUtils.addSeconds(new Date(), Math.toIntExact(-60));
    }

    /**
     * @return the configurationSetting
     */
    public ConfigurationSetting getConfigurationSetting() {
        return configurationSetting;
    }

    /**
     * @return the notCachedTime
     */
    public Date getNotCachedTime() {
        return notCachedTime;
    }

}
