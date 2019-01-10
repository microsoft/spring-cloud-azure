/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties.LABEL_SEPARATOR;

public class ConfigServiceTemplate implements ConfigServiceOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceTemplate.class);
    private static final String LINK_HEADER = "link";
    private static final String NEXT_PAGE_LINK = "<(.*?)>; rel=\"next\".*";
    private static final Pattern PAGE_LINK_PATTERN = Pattern.compile(NEXT_PAGE_LINK);

    public static final String LOAD_FAILURE_MSG = "Failed to load keys from Azure Config Service.";
    public static final String LOAD_FAILURE_VERBOSE_MSG = LOAD_FAILURE_MSG + " With status code: %s, response: %s";

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ConfigHttpClient configClient;
    private final ConnectionStringPool connectionStringPool;
    private final Map<String, List<String>> storeLabelsMap = new ConcurrentHashMap<>();

    public ConfigServiceTemplate(ConfigHttpClient httpClient, ConnectionStringPool connectionStringPool) {
        this.configClient = httpClient;
        this.connectionStringPool = connectionStringPool;
    }

    @Override
    public List<KeyValueItem> getKeys(@Nullable String prefix, @NonNull ConfigStore store) {
        ConnectionString connString = connectionStringPool.get(store.getName());
        String storeEndpoint = connString.getEndpoint();
        List<String> labels = storeLabelsMap.computeIfAbsent(store.getName(), (k) -> getLabels(store));

        String requestUri = new RestAPIBuilder().withEndpoint(storeEndpoint).buildKVApi(prefix, getLabels(store));
        List<KeyValueItem> result = new ArrayList<>();

        CloseableHttpResponse response = null;
        try {
            response = getRawResponse(requestUri, connString);
            while (response != null) {
                try {
                    KeyValueResponse kvResponse = mapper.readValue(response.getEntity().getContent(),
                            KeyValueResponse.class);

                    List<KeyValueItem> items = kvResponse.getItems();
                    sortByLabel(items, labels);
                    result.addAll(items);
                } catch (IOException e) {
                    throw new IllegalStateException(LOAD_FAILURE_MSG, e);
                }

                String nextLink = getNextLink(response);
                if (!StringUtils.hasText(nextLink)) {
                    break;
                }

                String nextRequestUri = new RestAPIBuilder().withEndpoint(storeEndpoint).withPath(nextLink)
                        .buildKVApi();
                response = getRawResponse(nextRequestUri, connString);
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

    private void sortByLabel(List<KeyValueItem> items, List<String> labels) {
        if (items == null || items.size() <= 1 || labels == null || labels.size() <= 1) {
            return;
        }

        Map<String, Integer> labelIndex = new HashMap<>();
        Collections.sort(items, new Comparator<KeyValueItem>() {
            @Override
            public int compare(KeyValueItem o1, KeyValueItem o2) {
                Integer o1Index = labelIndex.computeIfAbsent(getLabelValue(o1), (t) -> labels.indexOf(t));
                Integer o2Index = labelIndex.computeIfAbsent(getLabelValue(o2), (t) -> labels.indexOf(t));
                return o1Index - o2Index;
            }
        });
    }

    private String getLabelValue(KeyValueItem item) {
        if (StringUtils.hasText(item.getLabel())) {
            return item.getLabel();
        }

        return RestAPIBuilder.NULL_LABEL;
    }

    private CloseableHttpResponse getRawResponse(String requestUri, @NonNull ConnectionString connString) {
        HttpGet httpGet = new HttpGet(requestUri);
        Date date = new Date();

        LOGGER.debug("Loading key-value items from Azure Config service at [{}].", requestUri);
        try {
            CloseableHttpResponse response = configClient.execute(httpGet, date, connString.getId(),
                    connString.getSecret());
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                return response;
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                LOGGER.warn("No configuration data found in Azure Config Service for request uri {}.", requestUri);
                return null;
            } else {
                throw new IllegalStateException(String.format(LOAD_FAILURE_VERBOSE_MSG, statusCode, response));
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(LOAD_FAILURE_MSG, e);
            // TODO (wp) wrap exception as config service specific exception in order to provide fail-fast etc.
            // features?
            throw new IllegalStateException(LOAD_FAILURE_MSG, e);
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

    private static List<String> getLabels(ConfigStore store) {
        if (store == null || !StringUtils.hasText(store.getLabel())) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.stream(store.getLabel().split(LABEL_SEPARATOR))
                .filter(label -> StringUtils.hasText(label))
                .map(label -> label.trim())
                .distinct()
                .collect(Collectors.toList());
    }
}
