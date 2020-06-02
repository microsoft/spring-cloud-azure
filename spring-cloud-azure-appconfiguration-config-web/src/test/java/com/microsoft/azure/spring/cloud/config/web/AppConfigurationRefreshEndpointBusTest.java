/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.http.HttpStatus;

import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

public class AppConfigurationRefreshEndpointBusTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private BufferedReader reader;

    @Mock
    private Stream<String> lines;

    @Mock
    private BusPublisher busPublisher;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void webHookValidation() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenName(tokenName);
        properties.setTokenSecret(tokenSecret);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(busPublisher, properties);

        when(request.getReader()).thenReturn(reader);
        when(reader.lines()).thenReturn(lines);
        when(lines.collect(Mockito.any())).thenReturn("[{\r\n" +
                "  \"id\": \"2d1781af-3a4c-4d7c-bd0c-e34b19da4e66\",\r\n" +
                "  \"topic\": \"/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx\",\r\n" +
                "  \"subject\": \"\",\r\n" +
                "  \"data\": {\r\n" +
                "    \"validationCode\": \"512d38b6-c7b8-40c8-89fe-f46f9e9622b6\",\r\n" +
                "    \"validationUrl\": \"https://rp-eastus2.eventgrid.azure.net:553/eventsubscriptions/estest/validate?id=512d38b6-c7b8-40c8-89fe-f46f9e9622b6&t=2018-04-26T20:30:54.4538837Z&apiVersion=2018-05-01-preview&token=1A1A1A1A\"\r\n"
                +
                "  },\r\n" +
                "  \"eventType\": \"Microsoft.EventGrid.SubscriptionValidationEvent\",\r\n" +
                "  \"eventTime\": \"2018-01-25T22:12:19.4556811Z\",\r\n" +
                "  \"metadataVersion\": \"1\",\r\n" +
                "  \"dataVersion\": \"1\"\r\n" +
                "}]");

        assertEquals("{ \"validationResponse\": \"512d38b6-c7b8-40c8-89fe-f46f9e9622b6\"}",
                endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void webHookRefresh() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenName(tokenName);
        properties.setTokenSecret(tokenSecret);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(busPublisher, properties);

        when(request.getReader()).thenReturn(reader);
        when(reader.lines()).thenReturn(lines);
        when(lines.collect(Mockito.any())).thenReturn("[]");

        assertEquals(HttpStatus.OK.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void webHookRefreshNotFound() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenName(tokenName);
        properties.setTokenSecret(tokenSecret);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(null, properties);

        when(request.getReader()).thenReturn(reader);
        when(reader.lines()).thenReturn(lines);
        when(lines.collect(Mockito.any())).thenReturn("[]");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void noTokenName() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenSecret(tokenSecret);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(busPublisher, properties);

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void noTokenSecret() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenName(tokenName);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(busPublisher, properties);

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void noPramToken() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenName(tokenName);
        properties.setTokenSecret(tokenSecret);

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(busPublisher, properties);

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void invalidParamToken() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        String tokenName = "token";
        String tokenSecret = "secret";
        properties.setTokenName(tokenName);
        properties.setTokenSecret(tokenSecret);
        allRequestParams.put(tokenName, "noSecret");

        AppConfigurationRefreshBusEndpoint endpoint = new AppConfigurationRefreshBusEndpoint(busPublisher, properties);

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

}
