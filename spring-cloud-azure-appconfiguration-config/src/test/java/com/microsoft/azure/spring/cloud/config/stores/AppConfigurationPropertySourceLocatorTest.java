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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.microsoft.azure.spring.cloud.config.AppConfigurationProviderProperties;
import com.microsoft.azure.spring.cloud.config.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.AppConfigurationPropertySourceLocator;
import com.microsoft.azure.spring.cloud.config.KeyVaultCredentialProvider;

public class AppConfigurationPropertySourceLocatorTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private AppConfigurationProviderProperties appProperties;

    @Mock
    private AppConfigurationProperties properties;

    @Mock
    private ClientStore clients;

    @Mock
    private ConfigStore configStore;

    private AppConfigurationPropertySourceLocator azureConfigPropertySourceLocator;

    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void awaitOnError() throws Exception {
        expected.expect(Exception.class);
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        configStores.add(configStore);
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setProfileSeparator("_");
        properties.setName("TestStoreName");
        properties.setStores(configStores);

        appProperties.setPrekillTime(5);

        Environment env = Mockito.mock(ConfigurableEnvironment.class);
        String[] array = {};
        when(env.getActiveProfiles()).thenReturn(array);
        String[] labels = { "" };
        when(configStore.getLabels()).thenReturn(labels);
        when(configStore.isFailFast()).thenReturn(true);
        when(clients.listSettings(Mockito.any(), Mockito.any())).thenThrow(new NullPointerException(""));
        when(appProperties.getPrekillTime()).thenReturn(-60);
        when(appProperties.getStartDate()).thenReturn(new Date());

        azureConfigPropertySourceLocator = new AppConfigurationPropertySourceLocator(properties, appProperties, clients,
                tokenCredentialProvider);

        azureConfigPropertySourceLocator.locate(env);
        verify(appProperties, times(1)).getPrekillTime();
    }

}
