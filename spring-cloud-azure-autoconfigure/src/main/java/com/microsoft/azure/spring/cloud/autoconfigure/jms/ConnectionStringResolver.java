/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import java.util.Hashtable;

public class ConnectionStringResolver {
    private Hashtable hashtable;

    ConnectionStringResolver(String connectionString) {
        resolve(connectionString);
    }

    private void resolve(String connectionString) {
        String[] segments = connectionString.split(";");
        hashtable = new Hashtable();

        for (String segment : segments) {
            int indexOfEqualSign = segment.indexOf("=");
            String key = segment.substring(0, indexOfEqualSign);
            String value = segment.substring(indexOfEqualSign + 1);
            hashtable.put(key, value);
        }

        String endpoint = (String) hashtable.get("Endpoint");
        String[] segmentsOfEndpoint = endpoint.split("/");
        String host = segmentsOfEndpoint[segmentsOfEndpoint.length - 1];
        hashtable.put("host", host);
    }

    public Hashtable getResolvedKeysAndValues() {
        return hashtable;
    }
}
