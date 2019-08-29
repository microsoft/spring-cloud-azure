/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import com.microsoft.azure.spring.cloud.config.mock.MockCloseableHttpResponse;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties.LABEL_SEPARATOR;
import static com.microsoft.azure.spring.cloud.config.ConfigServiceTemplate.LOAD_FAILURE_VERBOSE_MSG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class ConfigServiceTemplateTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private ConfigHttpClient configClient;

    @InjectMocks
    private ConfigServiceTemplate template;

    private ConnectionStringPool pool;
    private ConfigStore configStore;

    public List<KeyValueItem> testItems;

    private HttpEntity okEntity;

    private static final KeyValueItem item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1);
    private static final KeyValueItem item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2);
    private static final KeyValueItem item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3);

    private static final QueryOptions TEST_OPTIONS = new QueryOptions().withKeyNames(TEST_CONTEXT);

    private static final ProtocolVersion VERSION = new ProtocolVersion("HTTP", 1, 1);
    private static final StatusLine OK_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_OK, null);

    private static final StatusLine NOT_FOUND_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_NOT_FOUND, null);

    private static final StatusLine TOO_MANY_REQ_STATUS = new BasicStatusLine(VERSION, 429, null);
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    private static final String LINK_HEADER = "link";

    private static final String FAIL_REASON = "Failed to process the request.";
    private static final StatusLine FAIL_STATUS = new BasicStatusLine(VERSION, HttpStatus.SC_BAD_REQUEST, FAIL_REASON);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        pool = new ConnectionStringPool();
        configStore = new ConfigStore();
        testItems = new ArrayList<>();

        pool.put(TEST_STORE_NAME, ConnectionString.of(TEST_CONN_STRING));
        configStore.setName(TEST_STORE_NAME);
        configStore.setConnectionString(TEST_CONN_STRING);

        testItems.addAll(Arrays.asList(item1, item2, item3));

        okEntity = buildEntity(testItems);
    }

    @After
    public void cleanup() {
        pool = new ConnectionStringPool();
        configStore = new ConfigStore();
    }

    @Test
    public void testKeysCanBeSearched() throws IOException, URISyntaxException {
        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(OK_STATUS, okEntity));
        template = new ConfigServiceTemplate(configClient, pool);

        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS);
        assertThat(result.size()).isEqualTo(testItems.size());
        assertThat(result).containsExactlyInAnyOrder(testItems.stream().toArray(KeyValueItem[]::new));
    }

    @Test
    public void testSpecificLabelCanBeSearched() throws IOException, URISyntaxException {
        prepareConfigClient();

        template = new ConfigServiceTemplate(configClient, pool);
        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS.withLabels(TEST_LABEL_2));
        List<KeyValueItem> expectedResult = Arrays.asList(item2);
        assertThat(result.size()).isEqualTo(expectedResult.size());
        assertThat(result).containsExactly(expectedResult.stream().toArray(KeyValueItem[]::new));
    }

    private void prepareConfigClient() throws IOException, URISyntaxException {
        when(configClient.execute(any(), any(), any(), any())).thenAnswer(new Answer<CloseableHttpResponse>() {
            @Override
            public CloseableHttpResponse answer(InvocationOnMock invocation) throws Throwable {
                // Extract label params from the request argument and filter result from the given testItems
                Object[] args = invocation.getArguments();
                HttpUriRequest request = (HttpUriRequest) args[0];
                List<NameValuePair> params = URLEncodedUtils.parse(request.getURI(), Charset.defaultCharset());
                Optional<NameValuePair> labelParam = params.stream()
                        .filter(p -> LABEL_PARAM.equals(p.getName())).findFirst();

                if (!labelParam.isPresent()) {
                    return null;
                }

                String labelValue = labelParam.get().getValue();
                List<String> labels = Arrays.asList(labelValue.split(LABEL_SEPARATOR));
                List<KeyValueItem> result = testItems.stream().filter(item -> labels.contains(item.getLabel()))
                        .collect(Collectors.toList());

                return new MockCloseableHttpResponse(OK_STATUS, buildEntity(result));
            }
        });
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
        template.getKeys(TEST_STORE_NAME, new QueryOptions());
    }

    @Test
    public void notFoundReturnEmptyList() throws Exception {
        when(configClient.execute(any(), any(), any(), any()))
                .thenReturn(new MockCloseableHttpResponse(NOT_FOUND_STATUS, null));
        template = new ConfigServiceTemplate(configClient, pool);

        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void keysCanBeSearchedIfNextLinkExists() throws Exception {
        // Initialize first page request response
        String firstLinkHeader = "</kv?after=fake-after>; rel=\"next\", </kv?before=fake-before>; rel=\"prev\"";
        CloseableHttpResponse firstResponse = mock(CloseableHttpResponse.class);
        HttpEntity firstEntity = buildEntity(Arrays.asList(item1, item2));
        when(firstResponse.getFirstHeader(LINK_HEADER)).thenReturn(new BasicHeader(LINK_HEADER, firstLinkHeader));
        when(firstResponse.getStatusLine()).thenReturn(OK_STATUS);
        when(firstResponse.getEntity()).thenReturn(firstEntity);

        // Initialize second page request response
        CloseableHttpResponse secondResponse = mock(CloseableHttpResponse.class);
        HttpEntity secondEntity = buildEntity(Arrays.asList(item3));
        when(secondResponse.getFirstHeader(LINK_HEADER)).thenReturn(new BasicHeader(LINK_HEADER, null));
        when(secondResponse.getStatusLine()).thenReturn(OK_STATUS);
        when(secondResponse.getEntity()).thenReturn(secondEntity);

        when(configClient.execute(any(), any(), any(), any())).thenReturn(firstResponse).thenReturn(secondResponse);
        template = new ConfigServiceTemplate(configClient, pool);
        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS);

        verify(configClient, times(2)).execute(any(), any(), any(), any());
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).containsExactlyInAnyOrder(item1, item2, item3);
    }

    @Test
    public void tooManyRequestsCanBeRetried() throws Exception {
        // Initialize first too many requests request response
        String retryMilliSecs = "5000";
        CloseableHttpResponse firstResponse = mock(CloseableHttpResponse.class);
        when(firstResponse.getFirstHeader(RETRY_AFTER_MS_HEADER))
                .thenReturn(new BasicHeader(RETRY_AFTER_MS_HEADER, retryMilliSecs));
        when(firstResponse.getStatusLine()).thenReturn(TOO_MANY_REQ_STATUS);

        // Initialize second valid request response
        CloseableHttpResponse secondResponse = mock(CloseableHttpResponse.class);
        HttpEntity entity = buildEntity(Arrays.asList(item1, item2));
        when(secondResponse.getFirstHeader(LINK_HEADER)).thenReturn(new BasicHeader(LINK_HEADER, null));
        when(secondResponse.getStatusLine()).thenReturn(OK_STATUS);
        when(secondResponse.getEntity()).thenReturn(entity);

        when(configClient.execute(any(), any(), any(), any())).thenReturn(firstResponse).thenReturn(secondResponse);
        template = new ConfigServiceTemplate(configClient, pool);

        long start = System.currentTimeMillis();
        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS);
        long end = System.currentTimeMillis();

        verify(configClient, times(2)).execute(any(), any(), any(), any());
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsExactlyInAnyOrder(item1, item2);
        assertThat(end - start).isGreaterThanOrEqualTo(Long.valueOf(retryMilliSecs));
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
        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS);

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
        List<KeyValueItem> result = template.getKeys(configStore.getName(), TEST_OPTIONS);

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
