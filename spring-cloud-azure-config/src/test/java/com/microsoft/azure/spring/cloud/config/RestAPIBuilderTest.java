/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RestAPIBuilderTest {
    private static final String FAKE_ENDPOINT = "https://fake.config.store.io";
    private static final String FAKE_KEY = "fake_key";
    private static final List<String> FAKE_LABEL = Arrays.asList("fake_label");
    private static final List<String> MULTI_FAKE_LABELS = Arrays.asList(" fake_label_1 ", "fake_label_2 ", "  ");
    private static final String FAKE_PATH_QUERY = "/kv?fake-param=fake-value";
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
        builder.buildKVApi(null, Collections.EMPTY_LIST);
    }

    @Test
    public void endpointShouldNotBeEmpty() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint("  ").withPath(KV_API);

        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Endpoint should not be empty or null");
        builder.buildKVApi(null, Collections.EMPTY_LIST);
    }

    @Test
    public void kvAPIShouldInitPath() throws URISyntaxException {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT).withPath(null);
        String apiPath = builder.buildKVApi(null, Collections.EMPTY_LIST);
        URIBuilder uriBuilder = new URIBuilder(apiPath);

        Assert.assertTrue("KV API path is not empty", StringUtils.hasText(KV_API));
        Assert.assertEquals("Generated KV REST api has correct path", KV_API, uriBuilder.getPath());
    }

    @Test
    public void nullLabelCanBeQueried() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);
        String apiPath = builder.buildKVApi(null, Collections.EMPTY_LIST);

        Assert.assertTrue("Null label should have query param %00.", apiPath.endsWith(NULL_LABEL_QUERY));
    }

    @Test
    public void nullLabelValueQueriedAsEmptyLabel() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);

        String apiPath = builder.buildKVApi(null, Collections.EMPTY_LIST);
        Assert.assertTrue("Empty label should have query param %00.", apiPath.endsWith(NULL_LABEL_QUERY));
    }

    @Test
    public void emptyStringLabelValueQueriedAsEmptyLabel() {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);

        String apiPath = builder.buildKVApi(null, Arrays.asList("   ", "  "));
        Assert.assertTrue("Whitespace consisted label should have query param %00.",
                apiPath.endsWith(NULL_LABEL_QUERY));
    }

    @Test
    public void bothKeyAndLabelCanBeConfigured() throws URISyntaxException {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);
        String apiPath = builder.buildKVApi(FAKE_KEY, FAKE_LABEL);
        URIBuilder uriBuilder = new URIBuilder(apiPath);

        List<String> keyParams = getParamValuesFrom(uriBuilder, KEY_PARAM);
        List<String> labelParams = getParamValuesFrom(uriBuilder, LABEL_PARAM);

        Assert.assertEquals("Only one key param exists.", 1, keyParams.size());
        Assert.assertEquals("Key param is created as expected.", FAKE_KEY, keyParams.get(0));

        Assert.assertEquals("Only one label param exists.", 1, labelParams.size());
        Assert.assertEquals("Label param is created as expected.", FAKE_LABEL.get(0), labelParams.get(0));
    }

    @Test
    public void multiLabelsCanBeConfigured() throws URISyntaxException {
        final RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT);
        String apiPath = builder.buildKVApi(FAKE_KEY, MULTI_FAKE_LABELS);
        URIBuilder uriBuilder = new URIBuilder(apiPath);

        List<String> keyParams = getParamValuesFrom(uriBuilder, KEY_PARAM);
        List<String> labelParams = getParamValuesFrom(uriBuilder, LABEL_PARAM);

        Assert.assertEquals("Only one key param exists.", 1, keyParams.size());
        Assert.assertEquals("Key param is created as expected.", FAKE_KEY, keyParams.get(0));

        Assert.assertEquals("Only one label param exists.", 1, labelParams.size());
        String expectedLabelParam = "fake_label_1,fake_label_2";
        Assert.assertEquals("The label param values are expected.", expectedLabelParam, labelParams.get(0));
    }

    @Test
    public void userCanConfigureWholePath() {
        RestAPIBuilder builder = new RestAPIBuilder().withEndpoint(FAKE_ENDPOINT).withPath(FAKE_PATH_QUERY);
        String actualPath = builder.buildKVApi();
        String expectedPath = FAKE_ENDPOINT + FAKE_PATH_QUERY;

        Assert.assertEquals("API path should be constructed from endpoint and path query.",
                expectedPath, actualPath);
    }

    private List<String> getParamValuesFrom(URIBuilder builder, String paramName) {
        return builder.getQueryParams().stream()
                .filter(pair -> pair.getName().equals(paramName))
                .map(pair -> pair.getValue()).collect(Collectors.toList());
    }
}
