/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import java.util.*;

/**
 * Collect service name and subscription, then return properties could be sent directly.
 *
 * @author Warren Zhu
 */
public class TelemetryCollector {
    private static final String PROJECT_VERSION = TelemetryCollector.class.getPackage().getImplementationVersion();

    private static final String PROJECT_INFO = "spring-cloud-azure/" + PROJECT_VERSION;

    private static final String VERSION = "version";

    private static final String INSTALLATION_ID = "installationId";

    private static final String SUBSCRIPTION_ID = "subscriptionId";

    private static final String SERVICE_NAME = "serviceName";
    private static final TelemetryCollector INSTANCE = new TelemetryCollector();
    private final String name = "spring-cloud-azure";
    private Set<String> services = new HashSet<>();
    private Map<String, String> properties = new HashMap<>();

    private TelemetryCollector() {
        this.buildProperties();
    }

    public static TelemetryCollector getInstance() {
        return INSTANCE;
    }

    public void addService(String service) {
        this.services.add(service);
    }

    public void setSubscription(String subscriptionId) {
        this.properties.put(SUBSCRIPTION_ID, subscriptionId);
    }

    public Collection<Map<String, String>> getProperties() {
        List<Map<String, String>> metrics = new LinkedList<>();

        for (String service : this.services) {
            Map<String, String> properties = new HashMap<>(this.properties);
            properties.put(SERVICE_NAME, service);
            metrics.add(properties);
        }

        return metrics;
    }

    private void buildProperties() {
        properties.put(VERSION, PROJECT_INFO);
        properties.put(INSTALLATION_ID, MacAddressHelper.getHashedMacAddress());
    }

    public String getName() {
        return this.name;
    }
}
