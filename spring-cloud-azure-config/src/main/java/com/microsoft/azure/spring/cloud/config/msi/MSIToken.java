/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.msi;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Type representing response from the MSI token provider.
 */
class MSIToken {
    private static DateTime epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeZone.UTC);

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "expires_on")
    private String expiresOn;

    String accessToken() {
        return accessToken;
    }

    String tokenType() {
        return tokenType;
    }

    boolean isExpired() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        DateTime expireOn = epoch.plusSeconds(Integer.parseInt(this.expiresOn));
        return now.isAfter(expireOn.getMillis());
    }
}
