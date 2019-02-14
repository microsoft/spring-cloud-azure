/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AzureConfigPropertySourceLocatorTest {
    private static final String APPLICATION_NAME = "foo";
    private static final String PROFILE_NAME_1 = "dev";
    private static final String PROFILE_NAME_2 = "prod";
    private static final String PREFIX = "/config";

    private static final KeyValueItem LABEL_1_ITEM_1 =
            createItem(TEST_DEFAULT_CONTEXT, LIST_KEY_1, TEST_VALUE_1, TEST_LABEL_1);
    private static final KeyValueItem LABEL_1_ITEM_2 =
            createItem(TEST_DEFAULT_CONTEXT, LIST_KEY_2, TEST_VALUE_2, TEST_LABEL_1);
    private static final KeyValueItem LABEL_2_ITEM_1 =
            createItem(TEST_DEFAULT_CONTEXT, LIST_KEY_1, TEST_VALUE_3, TEST_LABEL_2);

    private static final List<KeyValueItem> LABEL_1_ITEMS = Arrays.asList(LABEL_1_ITEM_1, LABEL_1_ITEM_2);
    private static final List<KeyValueItem> LABEL_2_ITEMS = Arrays.asList(LABEL_2_ITEM_1);

    @Rule
    public ExpectedException expected = ExpectedException.none();

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
        TestUtils.addStore(properties, TEST_STORE_NAME, TEST_CONN_STRING);
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
        String[] expectedSourceNames = new String[]{"/foo_prod/store1/%00", "/foo_dev/store1/%00", "/foo/store1/%00",
                "/application_prod/store1/%00", "/application_dev/store1/%00", "/application/store1/%00"};
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedSourceNames);
    }

    @Test
    public void compositeSourceIsCreatedForPrefixedConfig() {
        properties.getStores().get(0).setPrefix(PREFIX);
        locator = new AzureConfigPropertySourceLocator(operations, properties);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo, active profile: dev,prod and prefix: /config,
        // should construct below composite Property Source:
        // [/config/foo_prod/, /config/foo_dev/, /config/foo/, /config/application_prod/,
        // /config/application_dev/, /config/application/]
        String[] expectedSourceNames = new String[]{"/config/foo_prod/store1/%00", "/config/foo_dev/store1/%00",
                "/config/foo/store1/%00", "/config/application_prod/store1/%00", "/config/application_dev/store1/%00",
                "/config/application/store1/%00"};
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedSourceNames);
    }

    @Test
    public void nullApplicationNameCreateDefaultContextOnly() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("spring.application.name")).thenReturn(null);
        properties.setName(null);
        locator = new AzureConfigPropertySourceLocator(operations, properties);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Default context, null application name, empty active profile,
        // should construct composite Property Source: [/application/]
        String[] expectedSourceNames = new String[]{"/application/store1/%00"};
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedSourceNames);
    }

    @Test
    public void emptyApplicationNameCreateDefaultContextOnly() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("spring.application.name")).thenReturn("");
        properties.setName("");
        locator = new AzureConfigPropertySourceLocator(operations, properties);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Default context, empty application name, empty active profile,
        // should construct composite Property Source: [/application/]
        String[] expectedSourceNames = new String[]{"/application/store1/%00"};
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedSourceNames);
    }

    @Test
    public void defaultFailFastThrowException() {
        final String failureMsg = "Failed to load data from Azure Config Service.";
        expected.expect(RuntimeException.class);
        expected.expectMessage(failureMsg);

        when(operations.getKeys(any(), any())).thenThrow(new IllegalStateException(failureMsg));
        assertThat(properties.isFailFast()).isTrue();
        locator.locate(environment);
    }

    @Test
    public void notFailFastShouldPass() {
        properties.setFailFast(false);
        locator = new AzureConfigPropertySourceLocator(operations, properties);
        when(operations.getKeys(any(), any())).thenThrow(new IllegalStateException());

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);
    }

    @Test
    public void multiplePropertySourcesExistForMultiStores() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        properties = new AzureCloudConfigProperties();
        TestUtils.addStore(properties, TEST_STORE_NAME_1, TEST_CONN_STRING);
        TestUtils.addStore(properties, TEST_STORE_NAME_2, TEST_CONN_STRING_2);

        locator = new AzureConfigPropertySourceLocator(operations, properties);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        String[] expectedSourceNames = new String[]{"/application/" + TEST_STORE_NAME_2 + "/%00",
                "/application/" + TEST_STORE_NAME_1 + "/%00"};
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedSourceNames);
    }
}
