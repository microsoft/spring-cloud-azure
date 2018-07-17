/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TelemetryTracker {
    private static final Logger LOG = LoggerFactory.getLogger(TelemetryTracker.class);

    private static final String PROJECT_VERSION = TelemetryTracker.class.getPackage().getImplementationVersion();

    private static final String PROJECT_INFO = "spring-cloud-azure" + "/" + PROJECT_VERSION;

    private static final String PROPERTY_VERSION = "version";

    private static final String PROPERTY_INSTALLATION_ID = "installationId";

    private static final String PROPERTY_SUBSCRIPTION_ID = "subscriptionId";

    private static final String PROPERTY_SERVICE_NAME = "serviceName";

    private static final String SPRING_CLOUD_AZURE_EVENT = "spring-cloud-azure";

    private static final String TELEMETRY_INVALID_KEY = "invalid-instrumentationKey";

    private static final int INSTRUMENTATION_KEY_LENGTH = 36;

    private final TelemetryClient client;

    private final Map<String, String> defaultProperties;

    public TelemetryTracker(@NonNull String subscriptionId, String instrumentationKey) {
        this.defaultProperties = buildDefaultProperties(subscriptionId);
        this.client = buildTelemetryClient(instrumentationKey);
    }

    public static void triggerEvent(TelemetryTracker tracker, String serviceName) {
        if (tracker != null) {
            tracker.trackEventWithServiceName(serviceName);
        }
    }

    private Map<String, String> buildDefaultProperties(String subscriptionId) {
        Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_SUBSCRIPTION_ID, subscriptionId);
        properties.put(PROPERTY_VERSION, PROJECT_INFO);
        properties.put(PROPERTY_INSTALLATION_ID, MacAddressHelper.getHashedMacAddress());

        return Collections.unmodifiableMap(properties);
    }

    private TelemetryClient buildTelemetryClient(String instrumentationKey) {
        TelemetryClient client = new TelemetryClient();

        if (!isValid(instrumentationKey)) {
            LOG.warn("Telemetry instrumentationKey {} is invalid", instrumentationKey);
            throw new IllegalArgumentException("Telemetry instrumentationKey is invalid");
        }

        client.getContext().setInstrumentationKey(instrumentationKey);

        return client;
    }

    private boolean isValid(String instrumentationKey) {
        return StringUtils.hasText(instrumentationKey) && instrumentationKey.length() == INSTRUMENTATION_KEY_LENGTH;
    }

    private void trackEvent(@NonNull Map<String, String> customProperties) {
        if (!this.client.getContext().getInstrumentationKey().equals(TELEMETRY_INVALID_KEY)) {
            this.defaultProperties.forEach(customProperties::putIfAbsent);

            this.client.trackEvent(SPRING_CLOUD_AZURE_EVENT, customProperties, null);
            this.client.flush();
        }
    }

    private void trackEventWithServiceName(@NonNull String serviceName) {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_SERVICE_NAME, serviceName);

        this.trackEvent(properties);
    }
}
