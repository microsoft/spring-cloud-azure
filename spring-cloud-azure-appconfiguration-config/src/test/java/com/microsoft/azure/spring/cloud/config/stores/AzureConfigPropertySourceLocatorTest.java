/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.microsoft.azure.spring.cloud.config.AppConfigProviderProperties;
import com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties;
import com.microsoft.azure.spring.cloud.config.AzureConfigPropertySourceLocator;


public class AzureConfigPropertySourceLocatorTest {
    
    @Mock
    private AppConfigProviderProperties appProperties;

    @Mock
    private AzureCloudConfigProperties properties;
    
    @Mock
    private ClientStore clients;
    
    @Mock
    private ConfigStore configStore;
    
    private AzureConfigPropertySourceLocator azureConfigPropertySourceLocator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
    }
    
    @Test
    public void awaitOnError() throws Exception {
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        configStores.add(configStore);
        AzureCloudConfigProperties properties = new AzureCloudConfigProperties();
        properties.setProfileSeparator("_");
        properties.setName("TestStoreName");
        properties.setStores(configStores);
        
        appProperties.setPrekillTime(5);
        
        Environment env = Mockito.mock(ConfigurableEnvironment.class);
        String[] array = {};
        when(env.getActiveProfiles()).thenReturn(array);
        String[] labels = {""};
        when(configStore.getLabels()).thenReturn(labels);
        when(clients.listSettings(Mockito.any(), Mockito.any())).thenThrow(new ServerException(""));
        when(appProperties.getPrekillTime()).thenReturn(-60);
        when(appProperties.getStartDate()).thenReturn(new Date());
        
        azureConfigPropertySourceLocator = new AzureConfigPropertySourceLocator(properties, appProperties, clients);

        
        boolean threwException = false;
        try {
            azureConfigPropertySourceLocator.locate(env);
        } catch (Exception e) {
            threwException = true;
        }
        assertTrue(threwException);
        verify(appProperties, times(1)).getPrekillTime();
    }

}
