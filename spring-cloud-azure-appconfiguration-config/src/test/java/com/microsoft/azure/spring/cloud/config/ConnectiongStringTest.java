/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.resource.ConnectionString.ENDPOINT_ERR_MSG;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;

public class ConnectiongStringTest {
    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_ID_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void endpointMustExistInConnectionString() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(ENDPOINT_ERR_MSG);
        ConnectionString.of(NO_ENDPOINT_CONN_STRING);
    }

    @Test
    public void idMustExistInConnectionString() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(ENDPOINT_ERR_MSG);
        ConnectionString.of(NO_ID_CONN_STRING);
    }

    @Test
    public void secretMustExistInConnectionString() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(ENDPOINT_ERR_MSG);
        ConnectionString.of(NO_SECRET_CONN_STRING);
    }

    @Test
    public void validConnectionStringCanBeExtracted() {
        ConnectionString connString = ConnectionString.of(TEST_CONN_STRING);
        assertConnStringFieldsValid(connString);
    }

    private void assertConnStringFieldsValid(ConnectionString connString) {
        assertThat(connString).isNotNull();
        assertThat(connString.getEndpoint()).isEqualTo("https://fake.test.config.io");
        assertThat(connString.getId()).isEqualTo("fake-conn-id");
        assertThat(connString.getSecret()).isEqualTo("ZmFrZS1jb25uLXNlY3JldA==");
    }
}
