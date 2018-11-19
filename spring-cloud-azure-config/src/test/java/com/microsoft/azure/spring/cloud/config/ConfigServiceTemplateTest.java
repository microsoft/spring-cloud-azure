/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import com.microsoft.azure.spring.cloud.config.mock.MockCloseableHttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.spring.cloud.config.ConfigServiceTemplate.LOAD_FAILURE_VERBOSE_MSG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_3;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_3;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ConfigServiceTemplateTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private ConfigHttpClient configClient;

    private ConfigServiceTemplate template;

    public static final List<KeyValueItem> TEST_ITEMS = new ArrayList<>();
    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1);
    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2);
    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3);
    private static final KeyValueResponse kvResponse = new KeyValueResponse();

    private static final ProtocolVersion VERSION = new ProtocolVersion("HTTP", 1, 1);
    private static final StatusLine OK_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_OK, null);
    private static final BasicHttpEntity OK_ENTITY = new BasicHttpEntity();

    private static final String FAIL_REASON = "Failed to process the request.";
    private static final StatusLine FAIL_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_BAD_REQUEST, FAIL_REASON);

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        TEST_ITEMS.add(item1);
        TEST_ITEMS.add(item2);
        TEST_ITEMS.add(item3);
        kvResponse.setItems(TEST_ITEMS);

        ObjectMapper mapper = new ObjectMapper();
        OK_ENTITY.setContent(new ByteArrayInputStream(mapper.writeValueAsBytes(kvResponse)));
    }

    @Test
    public void testKeysCanBeSearched() throws IOException, URISyntaxException {
        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(OK_STATUS, OK_ENTITY));
        template = new ConfigServiceTemplate(configClient, TEST_ENDPOINT, TEST_ID, TEST_SECRET);

        List<KeyValueItem> result = template.getKeys(TEST_CONTEXT, null);
        assertThat(result.size()).isEqualTo(TEST_ITEMS.size());
        assertThat(result).containsExactlyInAnyOrder(TEST_ITEMS.stream().toArray(KeyValueItem[]::new));
    }

    @Test
    public void searchFailureShouldThrowException() throws IOException, URISyntaxException {
        String failureMsg = String.format(LOAD_FAILURE_VERBOSE_MSG, FAIL_STATUS.getStatusCode(),
                FAIL_STATUS.getReasonPhrase());

        expected.expect(IllegalStateException.class);
        expected.expectMessage(failureMsg);

        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(FAIL_STATUS, null));

        template = new ConfigServiceTemplate(configClient, TEST_ENDPOINT, TEST_ID, TEST_SECRET);
        template.getKeys(TEST_CONTEXT, null);
    }
}
