/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyValueItemTest {
    private static final String LEGAL_ITEM_LIST = "{\n" +
            "    \"items\": [\n" +
            "        {\n" +
            "            \"etag\": \"CAZU4pn8YBFqeH3XTWp61LGdBmJ\",\n" +
            "            \"key\": \"/foo_item_0\",\n" +
            "            \"label\": \"v0\",\n" +
            "            \"content_type\": \"user defined\",\n" +
            "            \"value\": \"example value\",\n" +
            "            \"tags\": {\n" +
            "                \"test-tag\": \"tag-value\"\n" +
            "            },\n" +
            "            \"locked\": false,\n" +
            "            \"last_modified\": \"2018-12-01T00:01:02+00:00\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void legalResponseJsonCanBeConverted() throws Exception {
        KeyValueResponse response = MAPPER.readValue(LEGAL_ITEM_LIST, KeyValueResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getItems().size()).isEqualTo(1);
    }
}
