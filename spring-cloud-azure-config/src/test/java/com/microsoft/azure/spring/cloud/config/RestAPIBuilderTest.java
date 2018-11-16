/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class RestAPIBuilderTest {
    private static final String FAKE_ENDPOINT = "https://fake.config.store.io";
    private static final String FAKE_KEY = "fake_key";
    private static final String FAKE_LABEL = "fake_label";
    private static final String KEY_PARAM = "key";
    private static final String LABEL_PARAM = "label";
    private static final String KV_API = RestAPIBuilder.KEY_VALUE_API;
    private static final String NULL_LABEL_QUERY = LABEL_PARAM + "=%00";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void endpointShouldNotBeNull() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(null).withPath(KV_API);

        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Endpoint should not be empty or null");
        builder.buildKVApi(null, null);
    }

    @Test
    public void endpointShouldNotBeEmpty() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint("  ").withPath(KV_API);

        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Endpoint should not be empty or null");
        builder.buildKVApi(null, null);
    }

    @Test
    public void kvAPIShouldInitPath() throws URISyntaxException {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT).withPath(null);
        String apiPath = builder.buildKVApi(null, null);
        URIBuilder uriBuilder = new URIBuilder(apiPath);

        Assert.assertTrue("KV API path is not empty", StringUtils.hasText(KV_API));
        Assert.assertEquals("Generated KV REST api has correct path", KV_API, uriBuilder.getPath());
    }

    @Test
    public void nullLabelCanBeQueried() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);
        String apiPath = builder.buildKVApi(null, null);

        Assert.assertTrue("Null label should have query param %00.", apiPath.endsWith(NULL_LABEL_QUERY));
    }

    @Test
    public void bothKeyAndLabelCanBeConfigured() throws URISyntaxException {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);
        String apiPath = builder.buildKVApi(FAKE_KEY, FAKE_LABEL);
        URIBuilder uriBuilder = new URIBuilder(apiPath);

        List<NameValuePair> keyParams = getParamsFrom(uriBuilder, KEY_PARAM);
        List<NameValuePair> labelParams = getParamsFrom(uriBuilder, LABEL_PARAM);

        Assert.assertEquals("Only one key param exists.", 1, keyParams.size());
        Assert.assertEquals("Key param is created as expected.", FAKE_KEY, keyParams.get(0).getValue());

        Assert.assertEquals("Only one label param exists.", 1, labelParams.size());
        Assert.assertEquals("Label param is created as expected.", FAKE_LABEL, labelParams.get(0).getValue());
    }

    private List<NameValuePair> getParamsFrom(URIBuilder builder, String paramName) {
        return builder.getQueryParams().stream()
                .filter(pair -> pair.getName().equals(paramName)).collect(Collectors.toList());
    }
}
