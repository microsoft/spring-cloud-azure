/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.msi.AzureConfigMSIConnector;
import com.microsoft.azure.spring.cloud.config.msi.ConfigMSICredentials;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AzureConfigBootstrapConfiguration.class)
@PowerMockIgnore({"javax.net.ssl.*"})
public class AzureConfigBootstrapConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING))
            .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class));

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private ConfigMSICredentials msiCredentials;

    @Mock
    private AzureConfigMSIConnector msiConnector;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

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

    @Test
    public void msiEmptyConnectionStringShouldFail() throws Exception {
        whenNew(ConfigMSICredentials.class).withNoArguments().thenReturn(msiCredentials);
        whenNew(AzureConfigMSIConnector.class).withAnyArguments().thenReturn(msiConnector);
        when(msiCredentials.getToken(anyString())).thenReturn(MSI_TOKEN);

        when(msiConnector.getConnection()).thenReturn("");

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class));

        contextRunner.run(context -> {
                    try {
                        context.getBean(AzureCloudConfigProperties.class);
                    } catch (Exception e) {
                        assertThat(context).getFailure().hasCauseInstanceOf(BeanCreationException.class);
                        assertThat(context).getFailure().hasStackTraceContaining("Connection string cannot be empty");
                    }
                });
    }

    @Test
    public void msiNonEmptyConnectionStringShouldPass() throws Exception {
        whenNew(ConfigMSICredentials.class).withNoArguments().thenReturn(msiCredentials);
        whenNew(AzureConfigMSIConnector.class).withAnyArguments().thenReturn(msiConnector);
        when(msiCredentials.getToken(anyString())).thenReturn(MSI_TOKEN);

        when(msiConnector.getConnection()).thenReturn(TEST_CONN_STRING);

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class));

        contextRunner.run(context -> {
            assertThat(context.getBean(AzureCloudConfigProperties.class)).isNotNull();
            AzureCloudConfigProperties properties = context.getBean(AzureCloudConfigProperties.class);
            assertThat(properties.getConnectionString()).isEqualTo(TEST_CONN_STRING);
        });
    }
}
