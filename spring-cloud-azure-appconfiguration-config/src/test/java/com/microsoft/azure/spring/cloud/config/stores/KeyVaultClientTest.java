/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ENDPOINT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.reactivestreams.Publisher;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;

import ch.qos.logback.core.util.Duration;
import reactor.core.publisher.Mono;

public class KeyVaultClientTest {

    private KeyVaultClient clientStore;

    static TokenCredential tokenCredential;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private SecretAsyncClient clientMock;
    
    @Mock
    private TokenCredential credentialMock;
    
    @Mock
    private Mono<KeyVaultSecret> monoSecret;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private AzureCloudConfigProperties azureProperties;

    @Test(expected = IllegalArgumentException.class)
    public void multipleArguments() throws IOException, URISyntaxException {
        azureProperties = new AzureCloudConfigProperties();
        AppConfigManagedIdentityProperties msiProps = new AppConfigManagedIdentityProperties();
        msiProps.setClientId("testclientid");
        azureProperties.setManagedIdentity(msiProps);
        
        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";
        
        KeyVaultCredentialProvider provider = new KeyVaultCredentialProvider() {
            
            @Override
            public TokenCredential getKeyVaultCredential(String uri) {
                assertEquals("https://keyvault.vault.azure.net", uri);
                return credentialMock;
            }
        };
        
        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), provider);
                
        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        test.build();
        fail();
    }

    @Test
    public void configClientIdAuth() throws IOException, URISyntaxException {
        azureProperties = new AzureCloudConfigProperties();
        AppConfigManagedIdentityProperties msiProps = null;
        azureProperties.setManagedIdentity(msiProps);
        
        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";
        
        KeyVaultCredentialProvider provider = new KeyVaultCredentialProvider() {
            
            @Override
            public TokenCredential getKeyVaultCredential(String uri) {
                assertEquals("https://keyvault.vault.azure.net", uri);
                return credentialMock;
            }
        };

        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), provider);
        
        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();
        
        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);;

        test.build();
        
        when(clientMock.getSecret(Mockito.any(), Mockito.any()))
                .thenReturn(monoSecret);
        when(monoSecret.block(Mockito.any())).thenReturn(new KeyVaultSecret("", ""));
        
        assertNotNull(test.getSecret(new URI(keyVaultUri), 10));
        assertEquals(test.getSecret(new URI(keyVaultUri), 10).getName(), "");
    }

}
