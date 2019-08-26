/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestClient.Builder;

public class ClientStoreTest {

    @Mock
    Builder builderMock;

    @Mock
    RestClient restClientMock;

    @Mock
    KeyVaultClient keyVaultClientMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildKeyVaultClientsTest() throws Exception {
        AzureCloudConfigProperties properties = new AzureCloudConfigProperties();
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

        ClientStore clientStore = new ClientStore(properties);
        assertEquals(1, clientStore.getKeyVaultClients().size());
    }

}
