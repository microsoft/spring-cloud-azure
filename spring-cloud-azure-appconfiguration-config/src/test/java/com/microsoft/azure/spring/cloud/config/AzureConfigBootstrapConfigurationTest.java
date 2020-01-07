/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FAIL_FAST_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.STORE_NAME_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestClient.Builder;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AzureConfigBootstrapConfiguration.class)
@PowerMockIgnore({ "javax.net.ssl.*", "javax.crypto.*" })
public class AzureConfigBootstrapConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(STORE_NAME_PROP, TEST_STORE_NAME))
            .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class));

    @Mock
    private MSICredentials msiCredentials;
    
    @Mock
    private CloseableHttpResponse mockClosableHttpResponse;

    @Mock
    HttpEntity mockHttpEntity;

    @Mock
    InputStream mockInputStream;

    @Mock
    Builder builderMock;

    @Mock
    RestClient restClientMock;

    @Mock
    KeyVaultClient keyVaultClientMock;

    @Mock
    ObjectMapper mockObjectMapper;
    
    @Mock
    ClientStore clientStoreMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        try {
            PowerMockito.whenNew(ObjectMapper.class).withAnyArguments().thenReturn(mockObjectMapper);
            when(mockClosableHttpResponse.getStatusLine())
                    .thenReturn(new BasicStatusLine(new ProtocolVersion("", 0, 0), 200, ""));
            when(mockClosableHttpResponse.getEntity()).thenReturn(mockHttpEntity);
            when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
            
            whenNew(Builder.class).withNoArguments().thenReturn(builderMock);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void propertySourceLocatorBeanCreated() throws Exception {
        whenNew(ClientStore.class).withAnyArguments().thenReturn(clientStoreMock);
        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
                .run(context -> assertThat(context).hasSingleBean(AzureConfigPropertySourceLocator.class));
    }

    @Test
    public void clientsBeanCreated() throws Exception {
        whenNew(ClientStore.class).withAnyArguments().thenReturn(clientStoreMock);
        contextRunner.withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
                .run(context -> {
                    assertThat(context).hasSingleBean(ClientStore.class);
                });
    }
}
