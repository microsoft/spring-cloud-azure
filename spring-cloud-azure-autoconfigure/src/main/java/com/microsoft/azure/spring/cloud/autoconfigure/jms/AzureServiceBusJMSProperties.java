/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

@Validated
@ConfigurationProperties("spring.cloud.azure.servicebus.jms")
public class AzureServiceBusJMSProperties {

    private String connectionString;

    private String clientId;

    private String idleTimeout;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(connectionString) || !StringUtils.hasText(idleTimeout)) {
            throw new IllegalArgumentException("Both 'spring.cloud.azure.servicebus.jms.connection-string' and " +
                    "'spring.cloud.azure.servicebus.jms.idle-timeout' should be provided");
        }
    }
}
