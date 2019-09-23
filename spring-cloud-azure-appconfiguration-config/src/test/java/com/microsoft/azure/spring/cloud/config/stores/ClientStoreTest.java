/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestClient.Builder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClientStore.class})
@PowerMockIgnore({ "javax.net.ssl.*", "javax.crypto.*" })
public class ClientStoreTest {
    
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    
    @Mock
    private AzureCloudConfigProperties mockProperties;
    
    @Mock
    private AppConfigProviderProperties mockAppProperties;
    
    @Mock
    private HashMap<String, ConfigurationAsyncClient> mockConfigClients;

    @Mock
    private Builder builderMock;
    
    @Mock
    private ConfigurationClientBuilder configClientBuilder;

    @Mock
    private RestClient restClientMock;

    @Mock
    private KeyVaultClient keyVaultClientMock;
    
    @Mock
    private ConfigurationAsyncClient configClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
    }

    @Test
    public void buildKeyVaultClientsTest() throws Exception {
        AzureCloudConfigProperties properties = new AzureCloudConfigProperties();
        AppConfigProviderProperties appProperties = new AppConfigProviderProperties();
        List<KeyVaultStore> keyVaultStores = new ArrayList<KeyVaultStore>();
        KeyVaultStore keyVaultStore = new KeyVaultStore();
        keyVaultStore.setClientId("");
        keyVaultStore.setConnectionUrl("https://www.test.url");
        keyVaultStore.setDomain("");
        keyVaultStore.setSecret("");
        keyVaultStores.add(keyVaultStore);
        properties.setKeyVaultStoresStores(keyVaultStores);

        whenNew(Builder.class).withNoArguments().thenReturn(builderMock);
        whenNew(KeyVaultClient.class).withParameterTypes(RestClient.class).withArguments(Mockito.any(RestClient.class))
                .thenReturn(keyVaultClientMock);

        when(builderMock.withBaseUrl(Mockito.anyString())).thenReturn(builderMock);
        when(builderMock.withCredentials(Mockito.any())).thenReturn(builderMock);
        when(builderMock.withSerializerAdapter(Mockito.any())).thenReturn(builderMock);
        when(builderMock.withResponseBuilderFactory(Mockito.any())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(restClientMock);

        ClientStore clientStore = new ClientStore(properties, appProperties);
        assertEquals(1, clientStore.getKeyVaultClients().size());
    }
    
    @Test
    public void awaitOnError() throws Exception {
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        HttpResponseException httpException = new HttpResponseException("Test", httpResponse);
        HttpHeader httpHeader = new HttpHeader(RETRY_AFTER_MS_HEADER, "1");
        
        ConfigurationClientBuilder builder = Mockito.mock(ConfigurationClientBuilder.class);
        whenNew(ConfigurationClientBuilder.class).withNoArguments().thenReturn(builder);
        
        when(builder.addPolicy(Mockito.any())).thenReturn(builder);
        when(builder.buildAsyncClient()).thenReturn(configClient);
        when(configClient.getSetting(Mockito.anyString())).thenThrow(httpException);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(httpHeaders.get(Mockito.anyString())).thenReturn(httpHeader);
        
        AzureCloudConfigProperties properties = new AzureCloudConfigProperties();
        AppConfigProviderProperties appProperties = new AppConfigProviderProperties();
        
        ArrayList<ConfigStore> stores = new ArrayList<ConfigStore>();
        ConfigStore configStore = new ConfigStore();
        configStore.setName("TestStore");
        configStore.setConnectionString("Endpoint=http://test.io;Id=abcd-ef-gh:ijklmnopqrstuvwxyzAB;Secret=12345678910111213141516171819202+1222324252=");
        stores.add(configStore);
        properties.setStores(stores);
        
        appProperties.setMaxRetries(12);
        appProperties.setMaxRetryTime(60);
        appProperties.setPrekillTime(5);

        ClientStore clientStore = new ClientStore(properties, appProperties);
        try {
            clientStore.getSetting("", "TestStore");
        } catch (HttpResponseException e) {
            
        }
        verify(configClient, times(13)).getSetting(Mockito.anyString());
    }

}
