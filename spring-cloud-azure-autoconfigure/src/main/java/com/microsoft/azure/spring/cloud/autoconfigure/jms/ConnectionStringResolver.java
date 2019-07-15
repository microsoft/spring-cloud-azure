/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import java.util.HashMap;

public class ConnectionStringResolver {

    public static ServiceBusKey getServiceBusKey(String connectionString) {
        String[] segments = connectionString.split(";");
        HashMap<String, String> hashMap = new HashMap<>();

        for (String segment : segments) {
            int indexOfEqualSign = segment.indexOf("=");
            String key = segment.substring(0, indexOfEqualSign);
            String value = segment.substring(indexOfEqualSign + 1);
            hashMap.put(key, value);
        }

        final String endpointKey = "Endpoint";
        final String hostKey = "host";
        String endpoint = hashMap.get(endpointKey);
        String[] segmentsOfEndpoint = endpoint.split("/");
        String host = segmentsOfEndpoint[segmentsOfEndpoint.length - 1];
        hashMap.put(hostKey, host);

        ServiceBusKey serviceBusKey = new ServiceBusKey();
        for (String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            if(key.equals("host")) {
                serviceBusKey.setHost(value);
            }
            if(key.equals("SharedAccessKeyName")) {
                serviceBusKey.setSharedAccessKeyName(value);
            }
            if(key.equals("SharedAccessKey")) {
                serviceBusKey.setSharedAccessKey(value);
            }
        }

        return serviceBusKey;

    }

}
