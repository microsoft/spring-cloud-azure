/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Wraps functions provided by Azure Instance Metadata Service(IMDS)
 */
public class AzureInstanceMetadataService {
    private String apiVersion = "2018-02-01-preview";
    private String tokenEndpoint = "http://169.254.169.254/metadata/identity/oauth2/token";
    private String objectId;
    private String clientId;

    public String buildTokenUrl(String resource) {
        Assert.hasText(resource, "Target resource should not be empty.");

        StringBuilder params = new StringBuilder();
        try {
            params.append(String.format("api-version=%s&resource=%s", encodeFragment(this.apiVersion),
                    encodeFragment(resource)));

            if (this.objectId != null) {
                params.append(String.format("&object_id=%s", encodeFragment(this.objectId)));
            }
            if (this.clientId != null) {
                params.append(String.format("&client_id=%s", encodeFragment(this.clientId)));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to generate retrieve token url.", e);
        }

        return String.format(tokenEndpoint + "?%s", params.toString());
    }

    private String encodeFragment(String urlFragment) throws UnsupportedEncodingException {
        Assert.hasText(urlFragment, "Url fragment to be encoded can not be empty.");
        return URLEncoder.encode(urlFragment, "UTF-8");
    }

    public AzureInstanceMetadataService withApiVersion(String apiVersion) {
        Assert.hasText(apiVersion, "API version should not be emtpy.");
        this.apiVersion = apiVersion;
        return this;
    }

    public AzureInstanceMetadataService withTokenEndpoint(String endpoint) {
        Assert.isTrue(isValidUrl(endpoint), "Endpoint should be valid url.");
        this.tokenEndpoint = endpoint;
        return this;
    }

    private boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }

        return true;
    }

    public AzureInstanceMetadataService withObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    public AzureInstanceMetadataService withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
}
