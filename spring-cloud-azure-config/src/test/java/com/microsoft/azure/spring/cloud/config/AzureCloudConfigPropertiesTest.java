/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties.NON_EMPTY_MSG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureCloudConfigPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertiesTestConfiguration.class));
    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_ID_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";
    private static final String[] ILLEGAL_PREFIXES = {"/ config", "config"};
    private static final String[] ILLEGAL_PROFILE_SEPARATOR = {"/", "\\", "."};

    @Test
    public void validInputShouldCreatePropertiesBean() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING)).run(context -> {
           assertThat(context).hasSingleBean(AzureCloudConfigProperties.class);
        });
    }

    @Test
    public void connectionStringMustBeConfigured() {
        this.contextRunner.withPropertyValues().run(context -> {
            assertInvalidField(context, "connectionString");
        });
    }

    @Test
    public void endpointMustExistInConnectionString() {
        testConnStringFields(NO_ENDPOINT_CONN_STRING, "Endpoint");
    }

    @Test
    public void idMustExistInConnectionString() {
        testConnStringFields(NO_ID_CONN_STRING, "Id");
    }

    @Test
    public void secretMustExistInConnectionString() {
        testConnStringFields(NO_SECRET_CONN_STRING, "Secret");
    }

    private void testConnStringFields(String connString, String fieldName) {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, connString)).run(context -> {
            assertThat(context).getFailure().hasStackTraceContaining(String.format(NON_EMPTY_MSG, fieldName));
        });
    }

    @Test
    public void defaultContextShouldNotBeEmpty() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(DEFAULT_CONTEXT_PROP, "")).run(context -> {
            assertInvalidField(context, "defaultContext");
        });
    }

    @Test
    public void prefixShouldFollowPattern() {
        Arrays.asList(ILLEGAL_PREFIXES).stream().forEach(prefix -> {
            this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(PREFIX_PROP, prefix)).run(context -> {
                assertInvalidField(context, "prefix");
            });
        });
    }

    @Test
    public void profileSeparatorShouldFollowPattern() {
        Arrays.asList(ILLEGAL_PROFILE_SEPARATOR).stream().forEach(separator -> {
            this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(SEPARATOR_PROP, separator)).run(context -> {
                        assertInvalidField(context, "profileSeparator");
            });
        });
    }

    private void assertInvalidField(AssertableApplicationContext context, String fieldName) {
        assertThat(context).getFailure().hasCauseInstanceOf(BindException.class);
        assertThat(context).getFailure()
                .hasStackTraceContaining(String.format("field '%s': rejected value", fieldName));
    }
}

@Configuration
@EnableConfigurationProperties(AzureCloudConfigProperties.class)
class PropertiesTestConfiguration {
    // Do nothing
}
