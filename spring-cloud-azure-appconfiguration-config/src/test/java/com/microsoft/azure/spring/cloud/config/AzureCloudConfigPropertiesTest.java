/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static com.microsoft.azure.spring.cloud.config.resource.ConnectionString.ENDPOINT_ERR_MSG;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureCloudConfigPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class));
    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_ID_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";
    private static final String[] ILLEGAL_PREFIXES = {"/ config", "config"};
    private static final String[] ILLEGAL_PROFILE_SEPARATOR = {"/", "\\", "."};
    private static final String ILLEGAL_LABELS = "*,my-label";

    @Test
    public void validInputShouldCreatePropertiesBean() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING)).run(context -> {
           assertThat(context).hasSingleBean(AzureCloudConfigProperties.class);
        });
    }

    @Test
    public void endpointMustExistInConnectionString() {
        testConnStringFields(NO_ENDPOINT_CONN_STRING);
    }

    @Test
    public void idMustExistInConnectionString() {
        testConnStringFields(NO_ID_CONN_STRING);
    }

    @Test
    public void secretMustExistInConnectionString() {
        testConnStringFields(NO_SECRET_CONN_STRING);
    }

    private void testConnStringFields(String connString) {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, connString)).run(context -> {
            assertThat(context).getFailure().hasStackTraceContaining(ENDPOINT_ERR_MSG);
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

    @Test
    public void asteriskShouldNotBeIncludedInTheLabels() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(LABEL_PROP, ILLEGAL_LABELS)).run(context -> {
            assertThat(context).getFailure()
                    .hasStackTraceContaining("Label must not contain asterisk(*)");
        });
    }

    @Test
    public void watchedKeyCanNotBeKeyPattern() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(WATCHED_KEY_PROP, TEST_WATCH_KEY_PATTERN)).run(context -> {
           assertThat(context).getFailure().hasStackTraceContaining("Watched key can only be a single asterisk(*) " +
            "or a specific key without asterisk(*)");
        });
    }

    @Test
    public void storeNameCanBeInitIfConnectionStringConfigured() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(STORE_NAME_PROP, "")).run(context -> {
            AzureCloudConfigProperties properties = context.getBean(AzureCloudConfigProperties.class);
            assertThat(properties.getStores()).isNotNull();
            assertThat(properties.getStores().size()).isEqualTo(1);
            assertThat(properties.getStores().get(0).getName()).isEqualTo("fake");
        });
    }

    @Test
    public void duplicateConnectionStringIsNotAllowed() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(CONN_STRING_PROP_NEW, TEST_CONN_STRING)).run(context -> {
            assertThat(context).getFailure().hasStackTraceContaining("Duplicate store name exists");
        });
    }

    private void assertInvalidField(AssertableApplicationContext context, String fieldName) {
        assertThat(context).getFailure().hasCauseInstanceOf(ConfigurationPropertiesBindException.class);
        assertThat(context).getFailure()
                .hasStackTraceContaining(String.format("field '%s': rejected value", fieldName));
    }
}

@Configuration
@EnableConfigurationProperties(AzureCloudConfigProperties.class)
class PropertiesTestConfiguration {
    // Do nothing
}
