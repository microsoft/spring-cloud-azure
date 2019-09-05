/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FAIL_FAST_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.STORE_NAME_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ACCESS_TOKEN;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.azure.identity.credential.ChainedTokenCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueResponse;
import com.microsoft.azure.spring.cloud.config.managed.identity.AzureResourceManagerConnector;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigServiceTemplate.class, AzureConfigBootstrapConfiguration.class })
@PowerMockIgnore({ "javax.net.ssl.*", "javax.crypto.*" })
public class AzureConfigBootstrapConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(STORE_NAME_PROP, TEST_STORE_NAME))
            .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class));
    
    @Mock
    private MSICredentials msiCredentials;

    @Mock
    private AzureResourceManagerConnector armConnector;

    @Mock
    private ConfigHttpClient configClient;

    @Mock
    private CloseableHttpResponse mockClosableHttpResponse;

    @Mock
    private HttpEntity mockHttpEntity;

    @Mock
    private InputStream mockInputStream;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        KeyValueResponse kvResponse = new KeyValueResponse();
        List<KeyValueItem> items = new ArrayList<KeyValueItem>();
        kvResponse.setItems(items);
        try {

            PowerMockito.whenNew(ConfigHttpClient.class).withAnyArguments().thenReturn(configClient);
            PowerMockito.whenNew(ObjectMapper.class).withAnyArguments().thenReturn(mockObjectMapper);
            when(configClient.execute(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(mockClosableHttpResponse);
            when(mockClosableHttpResponse.getStatusLine())
                    .thenReturn(new BasicStatusLine(new ProtocolVersion("", 0, 0), 200, ""));
            when(mockClosableHttpResponse.getEntity()).thenReturn(mockHttpEntity);
            when(mockHttpEntity.getContent()).thenReturn(mockInputStream);

            when(mockObjectMapper.readValue(Mockito.isA(InputStream.class), Mockito.any(Class.class)))
                    .thenReturn(kvResponse);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void closeableHttpClientBeanCreated() {
        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
                .run(context -> assertThat(context).hasSingleBean(CloseableHttpClient.class));
    }

    @Test
    public void configHttpClientBeanCreated() {
        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
                .run(context -> assertThat(context).hasSingleBean(ConfigHttpClient.class));
    }

    @Test
    public void configServiceOperationsBeanCreated() {
        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false")).run(context -> {
            assertThat(context).hasSingleBean(ConfigServiceOperations.class);
            assertThat(context.getBean(ConfigServiceOperations.class)).isExactlyInstanceOf(ConfigServiceTemplate.class);
        });
    }

    @Test
    public void propertySourceLocatorBeanCreated() {
        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
                .run(context -> assertThat(context).hasSingleBean(AzureConfigPropertySourceLocator.class));
    }

    @Test
    public void armEmptyConnectionStringShouldFail() throws Exception {
        whenNew(MSICredentials.class).withAnyArguments().thenReturn(msiCredentials);
        whenNew(AzureResourceManagerConnector.class).withAnyArguments().thenReturn(armConnector);

        when(msiCredentials.getToken(anyString())).thenReturn(TEST_ACCESS_TOKEN);
        when(armConnector.getConnectionString()).thenReturn("");

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class))
                .withPropertyValues(propPair(STORE_NAME_PROP, TestConstants.TEST_STORE_NAME));

        contextRunner.run(context -> {
            try {
                context.getBean(AzureCloudConfigProperties.class);
                Assert.fail("Empty connection string should fail.");
            } catch (Exception e) {
                assertThat(context).getFailure().hasCauseInstanceOf(BeanInstantiationException.class);
                assertThat(context).getFailure().hasStackTraceContaining("Connection string cannot be empty");
            }
        });
    }

    @Test
    public void armNonEmptyConnectionStringShouldPass() throws Exception {
        whenNew(MSICredentials.class).withAnyArguments().thenReturn(msiCredentials);
        whenNew(AzureResourceManagerConnector.class).withAnyArguments().thenReturn(armConnector);

        when(msiCredentials.getToken(anyString())).thenReturn(TEST_ACCESS_TOKEN);
        when(armConnector.getConnectionString()).thenReturn(TEST_CONN_STRING);

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class))
                .withPropertyValues(propPair(STORE_NAME_PROP, TEST_STORE_NAME));

        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false")).run(context -> {
            assertThat(context.getBean(ConnectionStringPool.class)).isNotNull();
            ConnectionStringPool pool = context.getBean(ConnectionStringPool.class);
            ConnectionString connString = pool.get(TEST_STORE_NAME);

            assertThat(connString).isNotNull();
            assertThat(connString.getEndpoint()).isEqualTo("https://fake.test.config.io");
            assertThat(connString.getId()).isEqualTo("fake-conn-id");
            assertThat(connString.getSecret()).isEqualTo("ZmFrZS1jb25uLXNlY3JldA==");
        });
    }
}
