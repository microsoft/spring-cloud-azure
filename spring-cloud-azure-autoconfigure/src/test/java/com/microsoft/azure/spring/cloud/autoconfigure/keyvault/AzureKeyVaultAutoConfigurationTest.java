/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.keyvault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureKeyVaultAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureKeyVaultAutoConfiguration.class));

    @Test
    public void testAzureKeyVaultDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.keyvault.enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultProperties.class));
    }

    @Test
    public void testWithoutRedisOperationsClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(KeyVaultClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultProperties.class));
    }

    @Test
    public void testKeyVaultPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.keyvault.client-id=id")
                          .withPropertyValues("spring.cloud.azure.keyvault.client-secret=secret")
                          .withPropertyValues("spring.cloud.azure.keyvault.name=name").run(context -> {
            assertThat(context).hasSingleBean(AzureKeyVaultProperties.class);
            assertThat(context.getBean(AzureKeyVaultProperties.class).getClientId()).isEqualTo("id");
            assertThat(context.getBean(AzureKeyVaultProperties.class).getClientSecret()).isEqualTo("secret");
            assertThat(context.getBean(AzureKeyVaultProperties.class).getName()).isEqualTo("name");
        });
    }
}
