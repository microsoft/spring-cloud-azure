/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Managed Service Identity token based credentials for use with a REST Service Client.
 *
 * Modified from https://azure.github.io/azure-sdk-for-java/com/microsoft/azure/credentials/MSICredentials.html,
 * to support api version configuration, and removed VM extension support which will be deprecated.
 */
public class ConfigMSICredentials extends AzureTokenCredentials {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMSICredentials.class);

    private static final String RETRIEVE_IMDS_FAILURE_MSG =
            "Failed to retrieve token from Azure Instance Metadata Service.";
    private static final int[] RETRY_SLOTS = new int[]{0, 2, 6, 14, 30}; /* Seconds */
    private static final int MAX_RETRY = RETRY_SLOTS.length;
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final AzureJacksonAdapter ADAPTER = new AzureJacksonAdapter();

    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<String, MSIToken> cache = new ConcurrentHashMap<>();
    private final String resource;
    private final AzureCloudConfigMSIProperties msiProperties;
    private AzureInstanceMetadataService metadataService;
    private ImmutableMap<String, String> tokenReqHeaders = ImmutableMap.of();
    private MSIType msiType;

    public ConfigMSICredentials(AzureCloudConfigMSIProperties msiProperties) {
        this(AzureEnvironment.AZURE, msiProperties);
    }

    /**
     * Initializes a new instance of the ConfigMSICredentials.
     *
     * @param environment the Azure environment to use
     */
    public ConfigMSICredentials(AzureEnvironment environment, AzureCloudConfigMSIProperties msiProperties) {
        super(environment, null); /* retrieving MSI token does not require tenant */
        this.resource = environment.managementEndpoint();
        this.msiProperties = msiProperties;
        this.msiType = checkMSIType();
        this.metadataService = getMetadataService(msiType);
        setHeader(msiType);
    }

    @Override
    public String getToken(String resource) {
        return this.getTokenFromIMDSEndpoint(resource == null ? this.resource : resource);
    }

    private String getTokenFromIMDSEndpoint(String resource) {
        MSIToken token = cache.get(resource);
        if (token != null && !token.isExpired()) {
            return token.getAccessToken();
        }
        lock.lock();
        try {
            token = cache.get(resource);
            if (token != null && !token.isExpired()) {
                return token.getAccessToken();
            }

            token = retrieveTokenFromIDMSWithRetry(resource);
            cache.put(resource, token);

            return token.getAccessToken();
        } finally {
            lock.unlock();
        }
    }

    private MSIToken retrieveTokenFromIDMSWithRetry(String resource) {
        HttpGet httpGet = new HttpGet(this.metadataService.buildTokenUrl(resource));
        this.tokenReqHeaders.forEach(httpGet::setHeader);

        CloseableHttpResponse response = null;

        int retry = 0;
        while (retry < MAX_RETRY) {
            try {
                LOGGER.debug("Acquiring token for resource {} from {} with headers [{}].", resource,
                        httpGet.getURI(),
                        Arrays.asList(httpGet.getAllHeaders())
                                .stream().map(header -> header.getName() + ":" + header.getValue())
                                .collect(Collectors.joining(";")));
                response = HTTP_CLIENT.execute(httpGet);
                return ADAPTER.deserialize(toString(response), MSIToken.class);
            } catch (Exception e) {
                LOGGER.error(RETRIEVE_IMDS_FAILURE_MSG, e);

                if (canRetry(response)) {
                    retryResponse(response, retry++);
                } else {
                    throw new IllegalStateException(RETRIEVE_IMDS_FAILURE_MSG);
                }
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        LOGGER.warn("Failed to close the response", e);
                    }
                }
            }
        }

        throw new IllegalStateException(RETRIEVE_IMDS_FAILURE_MSG);
    }

    private String toString(CloseableHttpResponse response) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
        } catch (IOException e) {
            LOGGER.warn("Failed to convert response {} to String.", response, e);
        }
        return writer.toString();
    }

    private boolean canRetry(CloseableHttpResponse response) {
        if (response == null) {
            return false;
        }

        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode == 404 || statusCode == 429 || (statusCode >= 500 && statusCode <= 599);
    }

    private void retryResponse(CloseableHttpResponse response, int retryIndex) {
        if (response == null) {
            return;
        }

        int statusCode = response.getStatusLine().getStatusCode();
        int sleepMilliSeconds = RETRY_SLOTS[retryIndex] * 1000;
        try {
            LOGGER.info("Will retry for status code {} after {} milli-seconds", statusCode, sleepMilliSeconds);
            Thread.sleep(sleepMilliSeconds);
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Failed to retry for response %s with status " +
                    "code %s.", toString(response), statusCode), e);
        }
    }


    private MSIType checkMSIType() {
        // Two types of MSI credential supported, TODO (w) may split implementation into different files
        if (StringUtils.hasText(System.getenv("MSI_ENDPOINT")) &&
                StringUtils.hasText(System.getenv("MSI_SECRET"))) {
            return MSIType.APP_SERVICE;
        } else {
            return MSIType.VM;
        }
    }

    private AzureInstanceMetadataService getMetadataService(MSIType type) {
        AzureInstanceMetadataService metadataService = new AzureInstanceMetadataService();
        if (msiProperties != null && StringUtils.hasText(msiProperties.getClientId())) {
            metadataService.withClientId(msiProperties.getClientId());
        } else if (msiProperties != null && StringUtils.hasText(msiProperties.getObjectId())) {
            metadataService.withObjectId(msiProperties.getObjectId());
        }

        switch (type) {
            case APP_SERVICE:
                metadataService.withTokenEndpoint(System.getenv("MSI_ENDPOINT"));
                break;
            default:
                break;
        }

        return metadataService;
    }

    private void setHeader(MSIType type) {
        switch (type) {
            case APP_SERVICE:
                tokenReqHeaders = ImmutableMap.of("Secret", System.getenv("MSI_SECRET"));
                break;
            default:
                tokenReqHeaders = ImmutableMap.of("Metadata", "true");;
        }
    }
}
