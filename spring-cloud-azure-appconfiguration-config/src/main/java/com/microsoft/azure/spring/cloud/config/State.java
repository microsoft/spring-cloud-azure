/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.Date;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

class State {
    private ConfigurationSetting configurationSetting;

    private Date lastCheckedTime;
    
    State(ConfigurationSetting configurationSetting) {
        this.configurationSetting = configurationSetting;
        lastCheckedTime = new Date();
    }

    /**
     * @return the configurationSetting
     */
    public ConfigurationSetting getConfigurationSetting() {
        return configurationSetting;
    }

    /**
     * @return the lastCheckedTime
     */
    public Date getLastCheckedTime() {
        return lastCheckedTime;
    }

}
