/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import com.microsoft.azure.spring.cloud.config.domain.QueryField;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import com.microsoft.azure.spring.cloud.config.domain.Range;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;

public class ConfigServiceTemplate implements ConfigServiceOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceTemplate.class);

    private static final String LINK_HEADER = "link";

    private static final String NEXT_PAGE_LINK = "<(.*?)>; rel=\"next\".*";

    private static final Pattern PAGE_LINK_PATTERN = Pattern.compile(NEXT_PAGE_LINK);

    private static final int TOO_MANY_REQ_CODE = 429;

    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";

    private static final String RANGE_HEADER = "Range";

    public static final String LOAD_FAILURE_MSG = "Failed to load keys from Azure Config Service.";

    public static final String LOAD_FAILURE_VERBOSE_MSG = LOAD_FAILURE_MSG + " With status code: %s, response: %s";

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ConfigHttpClient configClient;

    private final ConnectionStringPool connectionStringPool;
    
    private AppConfigProviderProperties appProperties;

    public ConfigServiceTemplate(ConfigHttpClient httpClient, ConnectionStringPool connectionStringPool,
            AppConfigProviderProperties appProperties) {
        this.configClient = httpClient;
        this.connectionStringPool = connectionStringPool;
        this.appProperties = appProperties;
    }

    @Override
    public List<KeyValueItem> getKeys(@NonNull String storeName, @NonNull QueryOptions options) {
        Assert.hasText(storeName, "Config store name should not be null or empty.");
        Assert.notNull(options, "The query options should not be null.");

        ConnectionString connString = connectionStringPool.get(storeName);
        String storeEndpoint = connString.getEndpoint();

        String requestUri = new RestAPIBuilder().withEndpoint(storeEndpoint).buildKVApi(options,
                appProperties.getVersion());

        return getKeys(requestUri, options, storeName);
    }

    @Override
    public List<KeyValueItem> getRevisions(@NonNull String storeName, @NonNull QueryOptions options) {
        Assert.hasText(storeName, "Config store name should not be null or empty.");
        Assert.notNull(options, "Query options should not be null or empty.");

        ConnectionString connString = connectionStringPool.get(storeName);
        String storeEndpoint = connString.getEndpoint();

        String requestUri = new RestAPIBuilder().withEndpoint(storeEndpoint).buildRevisionsApi(options,
                appProperties.getVersion());

        return getKeys(requestUri, options, storeName);
    }

    private List<KeyValueItem> getKeys(String requestUri, QueryOptions options, String storeName) {
        ConnectionString connString = connectionStringPool.get(storeName);
        String storeEndpoint = connString.getEndpoint();
        List<KeyValueItem> result = new ArrayList<>();

        CloseableHttpResponse response = null;
        try {
            while ((response = getRawResponse(requestUri, connString, options)) != null) {
                if (isThrottled(response)) {
                    throttleOnResponse(response);
                    continue;
                }

                try {
                    KeyValueResponse kvResponse = mapper.readValue(response.getEntity().getContent(),
                            KeyValueResponse.class);

                    List<KeyValueItem> items = kvResponse.getItems();
                    if (options.getSortField() != null && options.getSortField().equals(QueryField.LABEL)) {
                        sortByLabel(items, options.getLabelList());
                    }

                    result.addAll(items);
                } catch (IOException e) {
                    throw new IllegalStateException(LOAD_FAILURE_MSG, e);
                }

                if (itemsReachedRange(result, options.getRange())) {
                    break;
                }

                String nextLink = getNextLink(response);
                if (!StringUtils.hasText(nextLink)) {
                    break;
                }

                requestUri = new RestAPIBuilder().withEndpoint(storeEndpoint).withPath(nextLink)
                        .buildKVApi(appProperties.getVersion());
            }
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.warn("Response was not closed successfully.", e);
                }
            }
        }

        return result;
    }

    private boolean itemsReachedRange(List<KeyValueItem> items, Range range) {
        if (range == null) {
            return false;
        }

        int maxSize = range.getEndItem() - range.getStartItem() + 1;
        int itemsSize = items == null ? 0 : items.size();
        return itemsSize >= maxSize;
    }

    private boolean isThrottled(@NonNull CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode() == TOO_MANY_REQ_CODE;
    }

    private void throttleOnResponse(@NonNull CloseableHttpResponse response) {
        Header retryHeader = response.getFirstHeader(RETRY_AFTER_MS_HEADER);
        if (retryHeader == null || Long.valueOf(retryHeader.getValue()) <= 0) {
            throw new IllegalStateException(RETRY_AFTER_MS_HEADER + " header is missing or with illegal value for " +
                    "status code " + TOO_MANY_REQ_CODE);
        }

        long sleepMillSecs = Long.valueOf(retryHeader.getValue());
        try {
            LOGGER.debug("Will sleep {} milli-seconds as received too many requests response.", sleepMillSecs);
            Thread.sleep(sleepMillSecs);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to sleep for too many requests response.", e);
        }
    }

    private void sortByLabel(List<KeyValueItem> items, List<String> labels) {
        if (items == null || items.size() <= 1 || labels == null || labels.size() <= 1) {
            return;
        }

        Map<String, Integer> labelIndex = new HashMap<>();
        items.sort((o1, o2) -> {
            Integer o1Index = labelIndex.computeIfAbsent(getLabelValue(o1), (t) -> labels.indexOf(t));
            Integer o2Index = labelIndex.computeIfAbsent(getLabelValue(o2), (t) -> labels.indexOf(t));
            return o1Index - o2Index;
        });
    }

    private String getLabelValue(KeyValueItem item) {
        if (StringUtils.hasText(item.getLabel())) {
            return item.getLabel();
        }

        return RestAPIBuilder.NULL_LABEL;
    }

    private CloseableHttpResponse getRawResponse(String requestUri, @NonNull ConnectionString connString,
            QueryOptions options) {
        HttpGet httpGet = new HttpGet(requestUri);
        setRequestHeader(httpGet, options);
        Date date = new Date();

        LOGGER.debug("Loading key-value items from Azure Config service at [{}].", requestUri);
        try {
            CloseableHttpResponse response = configClient.execute(httpGet, date, connString.getId(),
                    connString.getSecret());
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT
                    || statusCode == TOO_MANY_REQ_CODE) {
                return response;
            }

            Header retryHeader = response.getFirstHeader(RETRY_AFTER_MS_HEADER);
            if (retryHeader != null && Long.parseLong(retryHeader.getValue()) > 0) {
                long retryValue = Long.parseLong(retryHeader.getValue());
                int retryCount = 0;
                Date maxRetryDate = DateUtils.addSeconds(date, appProperties.getMaxRetryTime());

                while (retryCount < appProperties.getMaxRetries() && !date.after(maxRetryDate)) {
                    try {
                        // Need to wait before Retry, need to figure out how long.
                        long retryBackoff = (new Double(Math.pow(2, retryCount))).longValue() - 1;
                        // Adds Jitter to unsync possible concurrent retry attempts
                        long jitter = 0;
                        if (retryBackoff != 0) {
                            jitter = retryValue * ThreadLocalRandom.current().nextLong(0, retryBackoff);
                        }
                        // Minimum sleep time retry is the Server returned value
                        Thread.sleep(Math.max(retryValue, jitter));
                    } catch (InterruptedException e) {
                        LOGGER.error("Failed to wait before retry.", e);
                    }
                    response = configClient.execute(httpGet, date, connString.getId(),
                            connString.getSecret());

                    statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT
                            || statusCode == TOO_MANY_REQ_CODE || statusCode == HttpStatus.SC_NOT_MODIFIED) {
                        return response;
                    }
                    retryCount++;
                    retryHeader = response.getFirstHeader(RETRY_AFTER_MS_HEADER);
                    if (retryHeader != null && Long.valueOf(retryHeader.getValue()) <= 0) {
                        throw new IllegalStateException(
                                RETRY_AFTER_MS_HEADER + " header is missing or with illegal value for " +
                                        "status code " + statusCode);
                    }
                    retryValue = Long.valueOf(retryHeader.getValue());
                }
            }   

            if (statusCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                LOGGER.warn("The range {} is not satisfiable for request uri {}.", options.getRange(), requestUri);
                return null;
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                LOGGER.warn("No configuration data found in Azure Config Service for request uri {}.", requestUri);
                return null;
            } else {
                delayException();
                throw new IllegalStateException(String.format(LOAD_FAILURE_VERBOSE_MSG, statusCode, response));
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(LOAD_FAILURE_MSG, e);
            delayException();
            throw new IllegalStateException(LOAD_FAILURE_MSG, e);
        }
    }
    
    private void delayException() {
        Date currentDate = new Date();
        Date maxRetryDate = DateUtils.addSeconds(appProperties.getStartDate(),
                appProperties.getPrekillTime());
        if (currentDate.before(maxRetryDate)) {
            long diffInMillies = Math.abs(maxRetryDate.getTime() - currentDate.getTime());
            try {
                Thread.sleep(diffInMillies);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait before fast fail.");
            }
        }
    }

    /**
     * Extract next link path and query from {@code response} header
     * @param response which response to extract link header from
     * @return matched next link header, return empty string if not found
     */
    private static String getNextLink(@NonNull CloseableHttpResponse response) {
        Header linkHeader = response.getFirstHeader(LINK_HEADER);
        if (linkHeader == null || !StringUtils.hasText(linkHeader.getValue())) {
            return "";
        }

        Matcher linkMatcher = PAGE_LINK_PATTERN.matcher(linkHeader.getValue());
        return linkMatcher.matches() ? linkMatcher.group(1) : "";
    }

    private void setRequestHeader(HttpGet httpGet, QueryOptions options) {
        if (options.getRange() != null) {
            httpGet.setHeader(RANGE_HEADER, options.getRange().toString());
        }
    }
}
