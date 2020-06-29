/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import static com.microsoft.azure.spring.cloud.config.web.Constants.VALIDATION_TOPIC;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring.PushNotification;
import com.microsoft.azure.spring.cloud.config.properties.ConfigStore;

public class AppConfigurationEndpoint {

    private static final String CONFIG_STORE_TOPIC = "configurationstores";

    private final String endpoint;

    private final String store;

    private List<ConfigStore> configStores;

    private Map<String, String> allRequestParams;

    public AppConfigurationEndpoint(JsonNode request, List<ConfigStore> configStores,
            Map<String, String> allRequestParams) {
        this.configStores = configStores;
        this.allRequestParams = allRequestParams;

        JsonNode requestTopic = request.findValue(VALIDATION_TOPIC);
        if (requestTopic != null) {
            String topic = requestTopic.asText();
            store = topic.substring(topic.indexOf(CONFIG_STORE_TOPIC) + CONFIG_STORE_TOPIC.length() + 1);
            endpoint = "https://" + store + ".azconfig.io";
        } else {
            throw new IllegalArgumentException("Refresh request missing topic field.");
        }

    }

    public boolean authenticate() {
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint)) {
                PushNotification pushNotification = configStore.getMonitoring().getPushNotification();
                String primaryTokenName = pushNotification.getPrimaryToken().getName();
                String primaryTokenSecret = pushNotification.getPrimaryToken().getSecret();
                String secondaryTokenName = pushNotification.getSecondaryToken().getName();
                String secondaryTokenSecret = pushNotification.getSecondaryToken().getSecret();

                // One of these need to be set
                if (!((primaryTokenName != null && primaryTokenSecret != null)
                        || (secondaryTokenName != null && secondaryTokenSecret != null))) {
                    return false;
                }

                if (allRequestParams.containsKey(primaryTokenName)
                        && allRequestParams.get(primaryTokenName).equals(primaryTokenSecret)) {
                    return true;
                }
                if (allRequestParams.containsKey(secondaryTokenName)
                        && allRequestParams.get(secondaryTokenName).equals(secondaryTokenSecret)) {
                    return true;
                }

            }
        }
        return false;
    }

    public boolean triggerRefresh() {
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint) && configStore.getMonitoring().isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return the store
     */
    public String getStore() {
        return store;
    }

}
