/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyLoader {

    private static final String PROJECT_PROPERTY_FILE = "/META-INF/project.properties";
    private static final String APPLICATION_PROPERTY_FILE = "/application.properties";
    private static final String APPLICATION_YML_FILE = "/application.yml";

    public static String getProjectVersion() {
        return getPropertyByName("project.version", PROJECT_PROPERTY_FILE);
    }

    public static boolean isApplicationTelemetryAllowed() {
        String telemetryAllowed = getPropertyByName("spring.cloud.azure.telemetryAllowed", APPLICATION_PROPERTY_FILE);

        if (telemetryAllowed == null) {
            telemetryAllowed = getPropertyByName("telemetryAllowed", APPLICATION_YML_FILE);
        }

        if (telemetryAllowed == null) {
            return true;
        } else {
            return telemetryAllowed.equalsIgnoreCase("false") ? false : true;
        }
    }

    private static String getPropertyByName(@NonNull String name, @NonNull String filename) {
        final Properties properties = new Properties();
        final InputStream inputStream = PropertyLoader.class.getResourceAsStream(filename);

        if (inputStream == null) {
            return null;
        }

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            log.warning(String.format("Failed to load file %s to property, will omit IOException.", filename));
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warning(String.format("Unable to close file %s, will omit IOException.", filename));
            }
        }

        return properties.getProperty(name);
    }
}
