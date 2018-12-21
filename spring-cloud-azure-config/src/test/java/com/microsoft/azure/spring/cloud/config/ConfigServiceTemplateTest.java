/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import com.microsoft.azure.spring.cloud.config.mock.MockCloseableHttpResponse;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
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
import java.util.Arrays;
import java.util.List;

import static com.microsoft.azure.spring.cloud.config.ConfigServiceTemplate.LOAD_FAILURE_VERBOSE_MSG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_3;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_3;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ConfigServiceTemplateTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private ConfigHttpClient configClient;

    private ConfigServiceTemplate template;

    private static final ConnectionStringPool pool = new ConnectionStringPool();
    private static final ConfigStore configStore = new ConfigStore();
    public static final List<KeyValueItem> TEST_ITEMS = new ArrayList<>();
    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1);
    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2);
    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3);

    private static final ProtocolVersion VERSION = new ProtocolVersion("HTTP", 1, 1);
    private static final StatusLine OK_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_OK, null);
    private static HttpEntity okEntity;

    private static final StatusLine NOT_FOUND_STATUS =
            new BasicStatusLine(VERSION, HttpStatus.SC_NOT_FOUND, null);

    private static final String FAIL_REASON = "Failed to process the request.";
    private static final StatusLine FAIL_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_BAD_REQUEST, FAIL_REASON);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        pool.put(TEST_STORE_NAME, ConnectionString.of(TEST_CONN_STRING));
        configStore.setName(TEST_STORE_NAME);
        configStore.setConnectionString(TEST_CONN_STRING);

        TEST_ITEMS.add(item1);
        TEST_ITEMS.add(item2);
        TEST_ITEMS.add(item3);

        okEntity = buildEntity(TEST_ITEMS);
    }

    @Test
    public void testKeysCanBeSearched() throws IOException, URISyntaxException {
        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(OK_STATUS, okEntity));
        template = new ConfigServiceTemplate(configClient, pool);

        List<KeyValueItem> result = template.getKeys(TEST_CONTEXT, null, configStore);
        assertThat(result.size()).isEqualTo(TEST_ITEMS.size());
        assertThat(result).containsExactlyInAnyOrder(TEST_ITEMS.stream().toArray(KeyValueItem[]::new));
    }

    @Test
    public void searchFailureShouldThrowException() throws IOException, URISyntaxException {
        String failureMsg = String.format(LOAD_FAILURE_VERBOSE_MSG, FAIL_STATUS.getStatusCode(),
                new MockCloseableHttpResponse(FAIL_STATUS, null));

        expected.expect(IllegalStateException.class);
        expected.expectMessage(failureMsg);

        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(FAIL_STATUS, null));

        template = new ConfigServiceTemplate(configClient, pool);
        template.getKeys(TEST_CONTEXT, null, configStore);
    }

    @Test
    public void notFoundReturnEmptyList() throws Exception {
        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(NOT_FOUND_STATUS, null));
        template = new ConfigServiceTemplate(configClient, pool);

        List<KeyValueItem> result = template.getKeys(TEST_CONTEXT, null, configStore);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void keysCanBeSearchedIfNextLinkExists() throws Exception {
        // Initialize first page request response
        String firstLinkHeader = "</kv?after=fake-after>; rel=\"next\", </kv?before=fake-before>; rel=\"prev\"";
        CloseableHttpResponse firstResponse = mock(CloseableHttpResponse.class);
        HttpEntity firstEntity = buildEntity(Arrays.asList(item1, item2));
        when(firstResponse.getFirstHeader("link")).thenReturn(new BasicHeader("link", firstLinkHeader));
        when(firstResponse.getStatusLine()).thenReturn(OK_STATUS);
        when(firstResponse.getEntity()).thenReturn(firstEntity);

        // Initialize second page request response
        CloseableHttpResponse secondResponse = mock(CloseableHttpResponse.class);
        HttpEntity secondEntity = buildEntity(Arrays.asList(item3));
        when(secondResponse.getFirstHeader("link")).thenReturn(new BasicHeader("link", null));
        when(secondResponse.getStatusLine()).thenReturn(OK_STATUS);
        when(secondResponse.getEntity()).thenReturn(secondEntity);

        when(configClient.execute(any(), any(), any(), any())).thenReturn(firstResponse).thenReturn(secondResponse);
        template = new ConfigServiceTemplate(configClient, pool);
        List<KeyValueItem> result = template.getKeys(TEST_CONTEXT, null, configStore);

        verify(configClient, times(2)).execute(any(), any(), any(), any());
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).containsExactlyInAnyOrder(item1, item2, item3);
    }

    @Test
    public void firstPageReturnedIfNoNextLinkExist() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = buildEntity(Arrays.asList(item1, item2));
        when(response.getFirstHeader("link")).thenReturn(new BasicHeader("link", null));
        when(response.getStatusLine()).thenReturn(OK_STATUS);
        when(response.getEntity()).thenReturn(httpEntity);

        when(configClient.execute(any(), any(), any(), any())).thenReturn(response);
        template = new ConfigServiceTemplate(configClient, pool);
        List<KeyValueItem> result = template.getKeys(TEST_CONTEXT, null, configStore);

        verify(configClient, times(1)).execute(any(), any(), any(), any());
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsExactlyInAnyOrder(item1, item2);
    }

    @Test
    public void firstPageReturnedIfNoNextLinkMatched() throws Exception {
        String incorrectLinkHeader = "</kv?after=fake-after>; rel=\"fake-link\"";
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = buildEntity(Arrays.asList(item1, item2));
        when(response.getFirstHeader("link")).thenReturn(new BasicHeader("link", incorrectLinkHeader));
        when(response.getStatusLine()).thenReturn(OK_STATUS);
        when(response.getEntity()).thenReturn(httpEntity);

        when(configClient.execute(any(), any(), any(), any())).thenReturn(response);
        template = new ConfigServiceTemplate(configClient, pool);
        List<KeyValueItem> result = template.getKeys(TEST_CONTEXT, null, configStore);

        verify(configClient, times(1)).execute(any(), any(), any(), any());
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsExactlyInAnyOrder(item1, item2);
    }

    private static HttpEntity buildEntity(List<KeyValueItem> items) throws JsonProcessingException {
        BasicHttpEntity entity = new BasicHttpEntity();
        KeyValueResponse kvResponse = new KeyValueResponse();
        kvResponse.setItems(items);
        entity.setContent(new ByteArrayInputStream(OBJECT_MAPPER.writeValueAsBytes(kvResponse)));

        return entity;
    }
}
