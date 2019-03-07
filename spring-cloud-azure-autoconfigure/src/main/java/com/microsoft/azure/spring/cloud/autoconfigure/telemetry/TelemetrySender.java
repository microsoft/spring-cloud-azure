/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

public class TelemetrySender {
    private static final Logger log = LoggerFactory.getLogger(TelemetrySender.class);

    private static final int INSTRUMENTATION_KEY_LENGTH = 36;

    private static final String TELEMETRY_TARGET_URL = "https://dc.services.visualstudio.com/v2/track";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpHeaders HEADERS = new HttpHeaders();

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final int RETRY_LIMIT = 3; // Align the retry times with sdk

    private final TelemetryCollector collector;
    private final String instrumentKey;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        HEADERS.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString());
    }

    public TelemetrySender(String instrumentKey, @NonNull TelemetryCollector collector) {
        this.instrumentKey = getInstrumentationKey(instrumentKey);
        this.collector = collector;
        this.scheduler.scheduleAtFixedRate(this::sendEvent, 0, 1, TimeUnit.HOURS);
    }

    private static String getInstrumentationKey(String instrumentationKey) {
        if (!isValid(instrumentationKey)) {
            log.warn("Telemetry instrumentationKey {} is invalid", instrumentationKey);
            throw new IllegalArgumentException("Telemetry instrumentationKey is invalid");
        }

        return instrumentationKey;
    }

    private static boolean isValid(String instrumentationKey) {
        return StringUtils.hasText(instrumentationKey) && instrumentationKey.length() == INSTRUMENTATION_KEY_LENGTH;
    }

    private void sendEvent() {
        this.collector.getProperties().forEach((m) -> {
            log.info("Sending telemetry event with properties {}", m);

            sendTelemetryData(new TelemetryEventData(collector.getName(), m, instrumentKey));
        });
    }

    private static ResponseEntity<String> executeRequest(final TelemetryEventData eventData) {
        try {
            final HttpEntity<String> body = new HttpEntity<>(MAPPER.writeValueAsString(eventData), HEADERS);

            return REST_TEMPLATE.exchange(TELEMETRY_TARGET_URL, HttpMethod.POST, body, String.class);
        } catch (Exception ignore) {
            log.warn("Failed to exchange telemetry request, {}.", ignore.getMessage());
        }

        return null;
    }

    private static void sendTelemetryData(@NonNull TelemetryEventData eventData) {
        ResponseEntity<String> response = null;

        for (int i = 0; i < RETRY_LIMIT; i++) {
            response = executeRequest(eventData);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                return;
            }
        }

        if (response != null && response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to send telemetry data, response status code {}.", response.getStatusCode().toString());
        }
    }
}
