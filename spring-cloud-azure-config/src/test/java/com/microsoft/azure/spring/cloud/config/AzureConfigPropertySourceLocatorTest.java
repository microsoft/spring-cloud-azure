/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.Collection;

import static com.microsoft.azure.spring.cloud.config.TestConstants.VALID_CONN_STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AzureConfigPropertySourceLocatorTest {
    private static final String APPLICATION_NAME = "foo";
    private static final String PROFILE_NAME_1 = "dev";
    private static final String PROFILE_NAME_2 = "prod";
    private static final String PREFIX = "/config";

    @Mock
    private ConfigServiceOperations operations;

    @Mock
    private ConfigurableEnvironment environment;

    private AzureCloudConfigProperties properties;

    private AzureConfigPropertySourceLocator locator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(environment.getActiveProfiles()).thenReturn(new String[]{PROFILE_NAME_1, PROFILE_NAME_2});

        properties = new AzureCloudConfigProperties();
        properties.setConnectionString(VALID_CONN_STRING);
        properties.setName(APPLICATION_NAME);

        locator = new AzureConfigPropertySourceLocator(operations, properties);
    }

    @Test
    public void compositeSourceIsCreated() {
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo and active profile: dev,prod, should construct below composite Property Source:
        // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/, /application/]
        String[] expectedPrefixes = new String[]{"/foo_prod/", "/foo_dev/", "/foo/", "/application_prod/",
                "/application_dev/", "/application/"};
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedPrefixes);
    }

    @Test
    public void compositeSourceIsCreatedForPrefixedConfig() {
        properties.setPrefix(PREFIX);
        locator = new AzureConfigPropertySourceLocator(operations, properties);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo, active profile: dev,prod and prefix: /config,
        // should construct below composite Property Source:
        // [/config/foo_prod/, /config/foo_dev/, /config/foo/, /config/application_prod/,
        // /config/application_dev/, /config/application/]
        String[] expectedPrefixes = new String[]{"/config/foo_prod/", "/config/foo_dev/", "/config/foo/",
                "/config/application_prod/", "/config/application_dev/", "/config/application/"};
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedPrefixes);
    }
}
