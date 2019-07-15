/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import java.util.HashMap;

public class ConnectionStringResolver {
    private HashMap<String, String> hashMap;

    ConnectionStringResolver(String connectionString) {
        resolve(connectionString);
    }

    private void resolve(String connectionString) {
        String[] segments = connectionString.split(";");
        hashMap = new HashMap<>();

        for (String segment : segments) {
            int indexOfEqualSign = segment.indexOf("=");
            String key = segment.substring(0, indexOfEqualSign);
            String value = segment.substring(indexOfEqualSign + 1);
            hashMap.put(key, value);
        }

        String endpoint = hashMap.get("Endpoint");
        String[] segmentsOfEndpoint = endpoint.split("/");
        String host = segmentsOfEndpoint[segmentsOfEndpoint.length - 1];
        hashMap.put("host", host);
    }

    public HashMap<String, String> getResolvedKeysAndValues() {
        return hashMap;
    }
}
