/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.PageImpl;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.Credentials;
import com.microsoft.rest.RestException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultPropertySourceLocatorTest {
    private static final String CLIENT_ID = "fake-client-id";
    private static final String CLIENT_SECRET = "fake-client-secret";
    private static final String DEFAULT_PROFILE_0 = "default-profile-0";
    private static final String DEFAULT_PROFILE_1 = "default-profile-1";
    private static final String DEFAULT_PROFILE_2 = "default-profile-2";
    private static final String CUSTOM_PROFILE_0 = "custom-profile-0";
    private static final String CUSTOM_PROFILE_1 = "custom-profile-1";
    private static final String CUSTOM_PROFILE_2 = "custom-profile-2";
    private static final String VAULT_URL_TEMPLATE = "https://%s-%s.vault.azure.net";

    private static final KeyVaultClient client = mock(KeyVaultClient.class);
    private static final ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
    private static final Credentials credentials = new Credentials();

    @BeforeClass
    public static void setup() {
        credentials.setClientId(CLIENT_ID);
        credentials.setClientSecret(CLIENT_SECRET);

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{DEFAULT_PROFILE_0, DEFAULT_PROFILE_1, DEFAULT_PROFILE_2});

        when(client.listSecrets(anyString()))
                .thenReturn(new PagedList<SecretItem>(new PageImpl<>()) {
                    @Override
                    public Page<SecretItem> nextPage(String s) throws RestException {
                        return null;
                    }
                });
    }

    @Test
    public void testDefaultActiveProfiles() {
        final String appName = "app1";
        KeyVaultConfigProperties properties = new KeyVaultConfigProperties();
        properties.setCredentials(credentials);
        properties.setName(appName);

        KeyVaultPropertySourceLocator locator = new KeyVaultPropertySourceLocator(client, properties);
        PropertySource<?> propertySource = locator.locate(environment);

        assertThat(propertySource).isInstanceOf(CompositePropertySource.class);

        CompositePropertySource composite = (CompositePropertySource) propertySource;
        assertThat(composite.getName()).isEqualTo("azure-key-vault-config");

        Collection<PropertySource<?>> propertySources = composite.getPropertySources();
        assertThat(propertySources.size()).isEqualTo(3);

        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        assertThat(iterator.next().getName()).isEqualTo(String.format(VAULT_URL_TEMPLATE, appName, DEFAULT_PROFILE_0));
        assertThat(iterator.next().getName()).isEqualTo(String.format(VAULT_URL_TEMPLATE, appName, DEFAULT_PROFILE_1));
        assertThat(iterator.next().getName()).isEqualTo(String.format(VAULT_URL_TEMPLATE, appName, DEFAULT_PROFILE_2));
    }

    @Test
    public void testCustomActiveProfiles() {
        final String appName = "app2";
        KeyVaultConfigProperties properties = new KeyVaultConfigProperties();
        properties.setCredentials(credentials);
        properties.setName(appName);
        properties.setActiveProfiles(String.join(",", CUSTOM_PROFILE_0, CUSTOM_PROFILE_1, CUSTOM_PROFILE_2));

        KeyVaultPropertySourceLocator locator = new KeyVaultPropertySourceLocator(client, properties);
        PropertySource<?> propertySource = locator.locate(environment);

        assertThat(propertySource).isInstanceOf(CompositePropertySource.class);

        CompositePropertySource composite = (CompositePropertySource) propertySource;
        assertThat(composite.getName()).isEqualTo("azure-key-vault-config");

        Collection<PropertySource<?>> propertySources = composite.getPropertySources();
        assertThat(propertySources.size()).isEqualTo(3);

        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        assertThat(iterator.next().getName()).isEqualTo(String.format(VAULT_URL_TEMPLATE, appName, CUSTOM_PROFILE_0));
        assertThat(iterator.next().getName()).isEqualTo(String.format(VAULT_URL_TEMPLATE, appName, CUSTOM_PROFILE_1));
        assertThat(iterator.next().getName()).isEqualTo(String.format(VAULT_URL_TEMPLATE, appName, CUSTOM_PROFILE_2));
    }
}
