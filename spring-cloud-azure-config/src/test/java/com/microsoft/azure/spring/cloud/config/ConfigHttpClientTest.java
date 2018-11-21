/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ID;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KV_API;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(ConfigHttpClient.class)
public class ConfigHttpClientTest {
    private static final String DATE_FORMAT = "EEE, d MMM yyyy hh:mm:ss z";
    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private static final String TEST_DATE = "Mon, 19 Nov 2018 12:00:00 GMT";
    private static final String TEST_USER_AGENT = ConfigHttpClient.USER_AGENT;

    private static final Map<String, String> REQ_HEADERS = new HashMap<>();

    @Mock
    private CloseableHttpClient httpClient;

    private Date testDate;

    @BeforeClass
    public static void init() {
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        REQ_HEADERS.put("x-ms-date", TEST_DATE);
        REQ_HEADERS.put("x-ms-content-sha256", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
        REQ_HEADERS.put("Authorization", "HMAC-SHA256 Credential=fake-conn-id, SignedHeaders=x-ms-date;host;" +
                "x-ms-content-sha256, Signature=P1SJR6iQjGgDmoV8/utXdwFyj69nYpd1OLkH1B9xjl8=");
        REQ_HEADERS.put(HttpHeaders.USER_AGENT, TEST_USER_AGENT);
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        testDate = GMT_DATE_FORMAT.parse(TEST_DATE);
    }

    @Test
    public void testRequestHeadersForAuthConfigured() throws URISyntaxException, IOException {
        when(httpClient.execute(any())).thenAnswer(new Answer<CloseableHttpResponse>() {
            @Override
            public CloseableHttpResponse answer(InvocationOnMock invocation) throws Throwable{
                Object[] args = invocation.getArguments();
                HttpUriRequest request = (HttpUriRequest) args[0];
                Header[] headers = request.getAllHeaders();

                Map<String, String> actualHeaderMap = new HashMap<>();
                Arrays.stream(headers).forEach(h -> actualHeaderMap.put(h.getName(), h.getValue()));

                // Test the headers of the passed in request has been correctly configured before firing request.
                assertThat(REQ_HEADERS.size()).isEqualTo(4);
                REQ_HEADERS.forEach((k, v) -> {
                    assertThat(actualHeaderMap).containsEntry(k, v);
                });

                return null;
            }
        });

        ConfigHttpClient configHttpClient = new ConfigHttpClient(httpClient);

        HttpGet httpGet = new HttpGet(new URI(TEST_KV_API));
        configHttpClient.execute(httpGet, testDate, TEST_ID, TEST_SECRET);
    }
}


