/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.CONFIG_ENABLED_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.STORE_ENDPOINT_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class AppConfigurationAutoConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(STORE_ENDPOINT_PROP, TEST_STORE_NAME))
            .withConfiguration(AutoConfigurations.of(AppConfigurationBootstrapConfiguration.class,
                    AppConfigurationAutoConfiguration.class));

    @Test
    public void watchEnabledNotConfiguredShouldNotCreateWatch() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "true")).run(context -> {
            assertThat(context).hasSingleBean(ConfigListener.class);
        });
    }
}
