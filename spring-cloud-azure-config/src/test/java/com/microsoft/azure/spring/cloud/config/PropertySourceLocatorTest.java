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

public class PropertySourceLocatorTest {
    private static final String APPLICATION_NAME = "foo";
    private static final String PROFILE_NAME = "dev";
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
        when(environment.getActiveProfiles()).thenReturn(new String[]{PROFILE_NAME});

        properties = new AzureCloudConfigProperties();
        properties.setConnectionString(VALID_CONN_STRING);
        properties.setName(APPLICATION_NAME);

        locator = new AzureConfigPropertySourceLocator(operations, properties);
    }

    @Test
    public void compositeSourceIsCreated() {
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource)source).getPropertySources();
        // Application name: foo and active profile: dev, should construct below composite Property Source:
        // [/foo_dev/, /foo/, /application_dev/, /application/]
        String[] expectedPrefixes = new String[]{"/foo_dev/", "/foo/", "/application_dev/", "/application/"};
        assertThat(sources.size()).isEqualTo(4);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedPrefixes);
    }

    @Test
    public void compositeSourceIsCreatedForPrefixedConfig() {
        properties.setPrefix(PREFIX);
        locator = new AzureConfigPropertySourceLocator(operations, properties);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource)source).getPropertySources();
        // Application name: foo, active profile: dev and prefix: /config,
        // should construct below composite Property Source:
        // [/config/foo_dev/, /config/foo/, /config/application_dev/, /config/application/]
        String[] expectedPrefixes = new String[]{"/config/foo_dev/", "/config/foo/", "/config/application_dev/",
                "/config/application/"};
        assertThat(sources.size()).isEqualTo(4);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly(expectedPrefixes);
    }
}
