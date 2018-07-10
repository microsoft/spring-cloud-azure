/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.management.Azure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

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

    private static final String TELEMETRY_INVALID_KEY = "invalid-instrumentationKey";

    private static final int INSTRUMENTATION_KEY_LENGTH = 36;

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryTracker.class);

    private final TelemetryClient client;

    private final Map<String, String> defaultProperties;

    public TelemetryTracker(Azure azure, String resourceGroup, TelemetryProperties telemetryProperties) {
        this.client = this.getTelemetryClient(telemetryProperties);

        this.defaultProperties =  new HashMap<>();

        this.defaultProperties.put(PROPERTY_SUBSCRIPTION_ID, azure.getCurrentSubscription().subscriptionId());
        this.defaultProperties.put(PROPERTY_RESOURCE_GROUP, resourceGroup);
        this.defaultProperties.put(PROPERTY_VERSION, PROJECT_INFO);
        this.defaultProperties.put(PROPERTY_INSTALLATION_ID, TelemetryUtils.getHashMac());
    }

    private TelemetryClient getTelemetryClient(TelemetryProperties telemetryProperties) {
        final TelemetryClient client = new TelemetryClient();
        final String instrumentationKey = telemetryProperties.getInstrumentationKey();

        if (StringUtils.hasText(instrumentationKey) && instrumentationKey.length() == INSTRUMENTATION_KEY_LENGTH) {
            client.getContext().setInstrumentationKey(instrumentationKey);
        } else {
            client.getContext().setInstrumentationKey(TELEMETRY_INVALID_KEY);
            LOG.warn("Telemetry instrumentationKey {} is invalid", instrumentationKey);
        }

        return client;
    }

    private void trackEvent(@NonNull String name, @NonNull Map<String, String> customProperties) {
        if (!this.client.getContext().getInstrumentationKey().equals(TELEMETRY_INVALID_KEY)) {
            this.defaultProperties.forEach(customProperties::putIfAbsent);

            this.client.trackEvent(name, customProperties, null);
            this.client.flush();
        }
    }

    public void trackEventWithServiceName(@NonNull String eventName, @NonNull String serviceName) {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_SERVICE_NAME, serviceName);

        this.trackEvent(eventName, properties);
    }
}
