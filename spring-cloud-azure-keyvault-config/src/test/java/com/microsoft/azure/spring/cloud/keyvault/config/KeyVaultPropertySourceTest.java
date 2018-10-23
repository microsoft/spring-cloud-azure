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
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.rest.RestException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultPropertySourceTest {
    private static final String KEY_VAULT_URL = "https://spring-cloud.vault.azure.net";
    private static final String SECRET_BASE_URL = "https://spring-cloud.vault.azure.net/secrets/";

    private static final String SECRET_NOT_EXIST = "not-existed";
    private static final String SECRET_0 = "test-secret-0";
    private static final String SECRET_1 = "test-secret-1";
    private static final String SECRET_2 = "test-secret-2";
    private static final String SECRET_VALUE_0 = "test-secret-value-0";
    private static final String SECRET_VALUE_1 = "test-secret-value-1";
    private static final String SECRET_VALUE_2 = "test-secret-value-2";

    private final KeyVaultClient client = mock(KeyVaultClient.class);

    @Test
    public void testGetProperties() {
        final List<SecretItem> secrets = Arrays.asList(new SecretItem().withId(SECRET_BASE_URL + SECRET_0),
                                                       new SecretItem().withId(SECRET_BASE_URL + SECRET_1),
                                                       new SecretItem().withId(SECRET_BASE_URL + SECRET_2));

        when(client.listSecrets(anyString()))
                .thenReturn(new PagedList<SecretItem>(new PageImpl<SecretItem>().setItems(secrets)) {
                    @Override
                    public Page<SecretItem> nextPage(String s) throws RestException {
                        return null;
                    }
                });
        when(client.getSecret(KEY_VAULT_URL, SECRET_0)).thenReturn(new SecretBundle().withValue(SECRET_VALUE_0));
        when(client.getSecret(KEY_VAULT_URL, SECRET_1)).thenReturn(new SecretBundle().withValue(SECRET_VALUE_1));
        when(client.getSecret(KEY_VAULT_URL, SECRET_2)).thenReturn(new SecretBundle().withValue(SECRET_VALUE_2));

        final KeyVaultPropertySource propertySource = new KeyVaultPropertySource(KEY_VAULT_URL, client, KEY_VAULT_URL);
        final String[] propertyNames = propertySource.getPropertyNames();

        assertThat(propertyNames.length).isEqualTo(secrets.size());
        assertThat(propertyNames).containsAll(Arrays.asList(SECRET_0, SECRET_1, SECRET_2));
        assertThat(propertySource.getProperty(SECRET_0)).isEqualTo(SECRET_VALUE_0);
        assertThat(propertySource.getProperty(SECRET_1)).isEqualTo(SECRET_VALUE_1);
        assertThat(propertySource.getProperty(SECRET_2)).isEqualTo(SECRET_VALUE_2);

        assertThat(propertySource.getProperty(SECRET_0.replace('-', '.'))).isEqualTo(SECRET_VALUE_0);
        assertThat(propertySource.getProperty(SECRET_1.replace('-', '.'))).isEqualTo(SECRET_VALUE_1);
        assertThat(propertySource.getProperty(SECRET_2.replace('-', '.'))).isEqualTo(SECRET_VALUE_2);

        assertThat(propertySource.getProperty(SECRET_NOT_EXIST)).isNull();
    }
}
