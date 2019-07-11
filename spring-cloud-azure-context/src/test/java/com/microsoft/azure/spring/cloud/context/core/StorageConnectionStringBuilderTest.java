/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.context.core;

import com.microsoft.azure.spring.cloud.context.core.api.Environment;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;

/**
 * @author dsibilio
 */
public class StorageConnectionStringBuilderTest {

    private static final String PROTOCOL_FORMAT = "{0}={1};";
    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_KEY = "accountKey";
    private static final Environment ENVIRONMENT = Environment.GLOBAL;
    private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

    @Test
    public void protocolShouldBeHttpsIfSecureTransferEnabled() {
        StorageConnectionStringBuilder underTest = new StorageConnectionStringBuilder(true);
        String connectionString = underTest.build(ACCOUNT_NAME, ACCOUNT_KEY, ENVIRONMENT);
        assertThat(connectionString).contains(MessageFormat.format(PROTOCOL_FORMAT, DEFAULT_PROTOCOL, "https"));
    }
    
    @Test
    public void protocolShouldBeHttpIfSecureTransferDisabled() {
        StorageConnectionStringBuilder underTest = new StorageConnectionStringBuilder(false);
        String connectionString = underTest.build(ACCOUNT_NAME, ACCOUNT_KEY, ENVIRONMENT);
        assertThat(connectionString).contains(MessageFormat.format(PROTOCOL_FORMAT, DEFAULT_PROTOCOL, "http"));
    }

}
