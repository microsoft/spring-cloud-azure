/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import org.springframework.util.Assert;

/**
 * Azure storage resource utility class
 *
 * @author Warren Zhu
 */
final class AzureStorageUtils {

    private static final String BLOB_PROTOCOL_PREFIX = "blob://";
    private static final String PATH_DELIMITER = "/";

    private AzureStorageUtils() {
        // Avoid instantiation
    }

    static boolean isAzureStorageResource(String location) {
        Assert.notNull(location, "Location must not be null");
        return location.toLowerCase().startsWith(BLOB_PROTOCOL_PREFIX);
    }

    static String getContainerName(String location) {
        assertIsAzureStorageLocation(location);
        int containerEndIndex = assertContainerValid(location);
        return location.substring(BLOB_PROTOCOL_PREFIX.length(), containerEndIndex);
    }

    static String getBlobName(String location) {
        assertIsAzureStorageLocation(location);
        int containerEndIndex = assertContainerValid(location);

        if (location.endsWith(PATH_DELIMITER)) {
            return location.substring(++containerEndIndex, location.length() - 1);
        }

        return location.substring(++containerEndIndex, location.length());
    }

    private static void assertIsAzureStorageLocation(String location) {
        if (!isAzureStorageResource(location)) {
            throw new IllegalArgumentException(
                    String.format("The location '%s' is not a valid Azure blob location", location));
        }
    }

    private static int assertContainerValid(String location) {
        int containerEndIndex = location.indexOf(PATH_DELIMITER, BLOB_PROTOCOL_PREFIX.length());
        if (containerEndIndex == -1 || containerEndIndex == BLOB_PROTOCOL_PREFIX.length()) {
            throw new IllegalArgumentException(
                    String.format("The location '%s' does not contain a valid container name", location));
        }

        return containerEndIndex;
    }
}
