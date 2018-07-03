/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.management.Azure;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryProperties.*;

public class TelemetryTracker {

    private static final String PROJECT_VERSION = TelemetryTracker.class.getPackage().getImplementationVersion();

    private static final String PROJECT_INFO = "spring-cloud-azure" + "/" + PROJECT_VERSION;

    private TelemetryClient client;

    private Azure azure;

    public TelemetryTracker(Azure azure) {
        this.client = new TelemetryClient();
        this.azure = azure;
    }

    public void trackEvent(@NonNull String name, Map<String, String> customProperties) {
        final Map<String, String> properties;
        final Map<String, String> defaultProperties = this.getDefaultProperties();

        if (customProperties == null) {
            properties = defaultProperties;
        } else {
            defaultProperties.forEach(customProperties::putIfAbsent);
            properties = customProperties;
        }

        client.trackEvent(name, properties, null);
        client.flush();
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
        properties.put(PROPERTY_VERSION, PROJECT_INFO);
        properties.put(PROPERTY_INSTALLATION_ID, TelemetryUtils.getHashMac());

        return properties;
    }
}
