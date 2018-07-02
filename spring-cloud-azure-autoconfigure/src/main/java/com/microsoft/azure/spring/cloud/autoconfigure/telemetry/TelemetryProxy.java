/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.management.Azure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryProperties.*;

public class TelemetryProxy {

    private TelemetryClient client;
    private boolean isTelemetryAllowed;
    private static final String PROJECT_INFO = "spring-data-gremlin/" + PropertyLoader.getProjectVersion();

    @Autowired
    private Azure azure;

    public TelemetryProxy(boolean isTelemetryAllowed) {
        this.client = new TelemetryClient();
        this.isTelemetryAllowed = isTelemetryAllowed;
    }

    public void trackEvent(@NonNull String name, Map<String, String> customProperties) {
        if (this.isTelemetryAllowed) {
            Map<String, String> properties = this.getDefaultProperties();

            properties = this.mergeProperties(properties, customProperties);

            client.trackEvent(name, properties, null);
            client.flush();
        }
    }

    private Map<String, String> mergeProperties(@NonNull Map<String, String> defaultProperties,
                                                Map<String, String> customProperties) {
        final Map<String, String> merged = new HashMap<>();

        merged.putAll(defaultProperties);
        merged.putAll(customProperties);

        return merged;
    }

    public void trackEventWithServiceName(@NonNull String eventName, @NonNull String serviceName) {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_SERVICE_NAME, serviceName);

        this.trackEvent(eventName, properties);
    }

    private Map<String, String> getDefaultProperties() {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_SUBSCRIPTION_ID, this.azure.getCurrentSubscription().subscriptionId());
        properties.put(PROPERTY_RESOURCE_GROUP, this.azure.resourceGroups().toString());
        properties.put(PROPERTY_INSTALLATION_ID, GetHashMac.getHashMac());
        properties.put(PROPERTY_VERSION, PROJECT_INFO);

        return properties;
    }
}
