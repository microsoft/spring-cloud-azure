/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TelemetryTracker {

    private static final String PROJECT_VERSION = TelemetryTracker.class.getPackage().getImplementationVersion();

    private static final String PROJECT_INFO = "spring-cloud-azure" + "/" + PROJECT_VERSION;

    private static final String PROPERTY_VERSION = "version";

    private static final String PROPERTY_INSTALLATION_ID = "installationId";

    private static final String PROPERTY_SUBSCRIPTION_ID = "subscriptionId";

    private static final String PROPERTY_RESOURCE_GROUP = "resourceGroup";

    private static final String PROPERTY_SERVICE_NAME = "serviceName";

    private static final String SPRING_CLOUD_AZURE_EVENT = "spring-cloud-azure";

    private final TelemetryClient client = new TelemetryClient();

    private final Map<String, String> defaultProperties;

    public TelemetryTracker(@NonNull String subscriptionId, @NonNull String resourceGroup) {
        this.defaultProperties = buildDefaultProperties(subscriptionId, resourceGroup);
    }

    public static void triggerEvent(TelemetryTracker tracker, String serviceName) {
        if (tracker != null) {
            tracker.trackEventWithServiceName(SPRING_CLOUD_AZURE_EVENT, serviceName);
        }
    }

    private Map<String, String> buildDefaultProperties(String subscriptionId, String resourceGroup) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_SUBSCRIPTION_ID, subscriptionId);
        properties.put(PROPERTY_RESOURCE_GROUP, resourceGroup);
        properties.put(PROPERTY_VERSION, PROJECT_INFO);
        properties.put(PROPERTY_INSTALLATION_ID, MacAddressHelper.getHashedMacAddress());
        return Collections.unmodifiableMap(properties);
    }

    private void trackEvent(@NonNull String name, @NonNull Map<String, String> customProperties) {
        this.defaultProperties.forEach(customProperties::putIfAbsent);

        this.client.trackEvent(name, customProperties, null);
        this.client.flush();
    }

    public void trackEventWithServiceName(@NonNull String eventName, @NonNull String serviceName) {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_SERVICE_NAME, serviceName);

        this.trackEvent(eventName, properties);
    }
}
