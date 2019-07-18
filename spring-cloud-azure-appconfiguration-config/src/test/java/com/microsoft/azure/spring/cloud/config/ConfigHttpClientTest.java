/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ID;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KV_API;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigHttpClient.class })
@PowerMockIgnore({"javax.crypto.*", "com.sun.org.apache.xerces.*", "org.xml.*", "javax.xml.*", "org.mockito.*"})
public class ConfigHttpClientTest {
    private static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private static final String TEST_USER_AGENT = ConfigHttpClient.USER_AGENT;

    private static final String TEST_DATE_1 = "Mon, 19 Nov 2018 00:00:00 GMT";
    private static final String CLIENT_REQUEST_ID_1 = "39bb45d6-d973-4224-8281-00834f09e0ac";
    private static final String CONTENT_HASH_1 = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    // The signature is generated with body as empty string "", but not null string
    private static final String AUTH_HEADER_1 = "HMAC-SHA256 Credential=fake-conn-id, SignedHeaders=x-ms-date;host;" +
            "x-ms-content-sha256, Signature=D+oRT4KhIRHpCdLiahSwJmcWuHmvnaYjwAm0SAcHGqs=";

    private static final String TEST_DATE_2 = "Mon, 19 Nov 2018 12:00:00 GMT";
    private static final String CLIENT_REQUEST_ID_2 = "139465da-d671-4c32-adee-2832d15bec62";
    private static final String CONTENT_HASH_2 = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    private static final String AUTH_HEADER_2 = "HMAC-SHA256 Credential=fake-conn-id, SignedHeaders=x-ms-date;host;" +
            "x-ms-content-sha256, Signature=P1SJR6iQjGgDmoV8/utXdwFyj69nYpd1OLkH1B9xjl8=";

    private static final String TEST_DATE_3 = "Mon, 19 Nov 2018 18:00:00 GMT";
    private static final String CLIENT_REQUEST_ID_3 = "d325fa85-b980-4532-a122-ef4dec652353";
    private static final String CONTENT_HASH_3 = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    private static final String AUTH_HEADER_3 = "HMAC-SHA256 Credential=fake-conn-id, SignedHeaders=x-ms-date;host;" +
            "x-ms-content-sha256, Signature=llHdHiBSvwRQjCOTz77g59hksBqxtEo3aERgxIWS4Ns=";

    private static final String TEST_DATE_4 = "Mon, 19 Nov 2018 23:59:59 GMT";
    private static final String CLIENT_REQUEST_ID_4 = "3695c623-a9dc-4f04-a0bc-0005ecbb57c4";
    private static final String CONTENT_HASH_4 = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    private static final String AUTH_HEADER_4 = "HMAC-SHA256 Credential=fake-conn-id, SignedHeaders=x-ms-date;host;" +
            "x-ms-content-sha256, Signature=va31Qc4gkGyZsqmVU8UoKh85QsxnjeNx2G9uxM5hZq0=";

    private static final List<TestRequestData> REQUEST_DATA_LIST = new ArrayList<>();

    private static Date testDate1;
    private static Date testDate2;
    private static Date testDate3;
    private static Date testDate4;
    
    private static UUID uuid1 = UUID.fromString(CLIENT_REQUEST_ID_1);
    private static UUID uuid2 = UUID.fromString(CLIENT_REQUEST_ID_2);
    private static UUID uuid3 = UUID.fromString(CLIENT_REQUEST_ID_3);
    private static UUID uuid4 = UUID.fromString(CLIENT_REQUEST_ID_4);

    @BeforeClass
    public static void init() throws ParseException {
        Map<String, String> reqHeaders1 = getReqHeaders(TEST_DATE_1, CLIENT_REQUEST_ID_1, CONTENT_HASH_1,
                AUTH_HEADER_1);
        Map<String, String> reqHeaders2 = getReqHeaders(TEST_DATE_2, CLIENT_REQUEST_ID_2, CONTENT_HASH_2,
                AUTH_HEADER_2);
        Map<String, String> reqHeaders3 = getReqHeaders(TEST_DATE_3, CLIENT_REQUEST_ID_3, CONTENT_HASH_3,
                AUTH_HEADER_3);
        Map<String, String> reqHeaders4 = getReqHeaders(TEST_DATE_4, CLIENT_REQUEST_ID_4, CONTENT_HASH_4,
                AUTH_HEADER_4);

        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));

        testDate1 = GMT_DATE_FORMAT.parse(TEST_DATE_1);
        testDate2 = GMT_DATE_FORMAT.parse(TEST_DATE_2);
        testDate3 = GMT_DATE_FORMAT.parse(TEST_DATE_3);
        testDate4 = GMT_DATE_FORMAT.parse(TEST_DATE_4);

        REQUEST_DATA_LIST.add(new TestRequestData(testDate1, TEST_DATE_1, reqHeaders1));
        REQUEST_DATA_LIST.add(new TestRequestData(testDate2, TEST_DATE_2, reqHeaders2));
        REQUEST_DATA_LIST.add(new TestRequestData(testDate3, TEST_DATE_3, reqHeaders3));
        REQUEST_DATA_LIST.add(new TestRequestData(testDate4, TEST_DATE_4, reqHeaders4));
    }

    private static Map<String, String> getReqHeaders(String date, String uuid, String contentHash,
            String authorizationHeader) {
        Map<String, String> reqHeaderMap = new HashMap<>();
        reqHeaderMap.put("x-ms-date", date);
        reqHeaderMap.put("x-ms-client-request-id", uuid);
        reqHeaderMap.put("x-ms-content-sha256", contentHash);
        reqHeaderMap.put("Authorization", authorizationHeader);
        reqHeaderMap.put(HttpHeaders.USER_AGENT, TEST_USER_AGENT);

        return reqHeaderMap;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRequestHeadersForAuthConfigured() throws URISyntaxException, IOException {
        assertThat(REQUEST_DATA_LIST.size()).isEqualTo(4);
        PowerMockito.mockStatic(UUID.class);
        PowerMockito.when(UUID.randomUUID()).thenReturn(uuid1, uuid2, uuid3, uuid4);
        for (int index = 0; index < REQUEST_DATA_LIST.size(); index++) {
            TestRequestData reqData = REQUEST_DATA_LIST.get(index);

            CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
            
            when(httpClient.execute(any())).thenAnswer(new Answer<CloseableHttpResponse>() {
                @Override
                public CloseableHttpResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    HttpUriRequest request = (HttpUriRequest) args[0];
                    Header[] headers = request.getAllHeaders();

                    Map<String, String> actualHeaderMap = new HashMap<>();
                    Arrays.stream(headers).forEach(h -> actualHeaderMap.put(h.getName(), h.getValue()));

                    // Test the headers of the passed in request has been correctly
                    // configured before firing request.
                    assertThat(reqData.reqHeaders.size()).isEqualTo(5);
                    reqData.reqHeaders.forEach((k, v) -> {
                        assertThat(actualHeaderMap).containsEntry(k, v);
                    });

                    return null;
                }
            });

            ConfigHttpClient configHttpClient = new ConfigHttpClient(httpClient);

            HttpGet httpGet = new HttpGet(new URI(TEST_KV_API));
            configHttpClient.execute(httpGet, reqData.getDate(), TEST_ID, TEST_SECRET);
        }
    }

    static class TestRequestData {
        private Date date;

        private String dateString;

        private Map<String, String> reqHeaders;

        public TestRequestData(Date date, String dateString, Map<String, String> reqHeaders) {
            this.date = date;
            this.dateString = dateString;
            this.reqHeaders = reqHeaders;
        }

        public Date getDate() {
            return date;
        }

        public String getDateString() {
            return dateString;
        }

        public Map<String, String> getReqHeaders() {
            return reqHeaders;
        }
    }
}
