/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureConfigBootstrapConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class))
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING));

    @Test
    public void closeableHttpClientBeanCreated() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(CloseableHttpClient.class));
    }

    @Test
    public void configHttpClientBeanCreated() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(ConfigHttpClient.class));
    }

    @Test
    public void configServiceOperationsBeanCreated() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConfigServiceOperations.class);
            assertThat(context.getBean(ConfigServiceOperations.class)).isExactlyInstanceOf(ConfigServiceTemplate.class);
        });
    }

    @Test
    public void propertySourceLocatorBeanCreated() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(AzureConfigPropertySourceLocator.class));
    }
}
