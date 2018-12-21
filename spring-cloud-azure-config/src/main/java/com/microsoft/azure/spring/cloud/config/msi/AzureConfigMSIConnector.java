/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.microsoft.azure.spring.cloud.config.msi.ConfigAccessKeyResource.ARM_ENDPONT;

/**
 * Get connection string for given Azure Configuration Service config store from ARM with MSI access token.
 */
public class AzureConfigMSIConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureConfigMSIConnector.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
    private final ConfigMSICredentials msiCredentials;
    private final String configStoreName;

    public AzureConfigMSIConnector(ConfigMSICredentials credentials, String configStoreName) {
        this.msiCredentials = credentials;
        this.configStoreName = configStoreName;
    }

    public String getConnectionString() {
        String msiToken = msiCredentials.getToken(ARM_ENDPONT);
        ConfigAccessKeyResource keyResource = getKeyResource();
        String resourceId = keyResource.getResourceIdUrl();

        HttpPost post = new HttpPost(resourceId);
        post.setHeader("Authorization", "Bearer " + msiToken);

        LOGGER.debug("Acquiring connection string from endpoint {}.", post.getURI());
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (!(statusCode >= 200 && statusCode <= 299)) {
                switch (statusCode) {
                    case HttpStatus.SC_NOT_FOUND:
                        throw new IllegalStateException(String.format("The configuration store with name %s " +
                                "and id %s could not be found.", configStoreName, resourceId));
                    case HttpStatus.SC_UNAUTHORIZED:
                    case HttpStatus.SC_FORBIDDEN:
                        throw new IllegalStateException(String.format("No permission to access configuration store %s",
                                configStoreName));
                    default:
                        throw new IllegalStateException(String.format("Failed to retrieve access key " +
                                "for configuration store %s.", configStoreName));
                }
            }

            ConfigAccessKeys result = mapper.readValue(response.getEntity().getContent(), ConfigAccessKeys.class);
            return getConnString(configStoreName, result);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to retrieve access key " +
                    "for configuration store %s.", configStoreName), e);
        }
    }

    private static String getConnString(String configStoreName, ConfigAccessKeys result) {
        ConfigAccessKey key = result.getAccessKeyList().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Access key should exist for " +
                                "configuration store %s", configStoreName)));

        return key.getConnectionString();
    }

    private ConfigAccessKeyResource getKeyResource() {
        Tuple<String, String> resourceInfo = new ConfigResourceManager(msiCredentials).findStore(configStoreName);
        if (resourceInfo == null) {
            throw new IllegalStateException(String.format("No configure store with name %s found, access key " +
                    "cannot be retrieved.", configStoreName));
        }

        return new ConfigAccessKeyResource(resourceInfo.getFirst(), resourceInfo.getSecond(), configStoreName);
    }
}
