/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.pipline.policies;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;

public class BaseAppConfigurationPolicyTest {

    @Mock
    HttpPipelineCallContext contextMock;

    @Mock
    HttpPipelineNextPolicy nextMock;

    private static final String PRE_USER_AGENT = "PreExistingUserAgent";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processTest() throws MalformedURLException {
        URL url = new URL("https://www.test.url/link");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.header(HttpHeaders.USER_AGENT, "PreExistingUserAgent");
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        when(contextMock.httpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        String userAgent = contextMock.httpRequest().headers().get(HttpHeaders.USER_AGENT).value();
        assertEquals("null/null; " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch,Host=None",
                contextMock.httpRequest().headers().get("Correlation-Context").value());

    }

}
