/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.CredentialsProvider;
import com.microsoft.azure.spring.cloud.context.core.DefaultCredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Auto-config to provide default {@link CredentialsProvider} for all Azure services
 *
 * @author Warren Zhu
 */
@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(Azure.class)
@ConditionalOnProperty("spring.cloud.azure.enabled")
public class AzureContextAutoConfiguration {

    private final AzureProperties azureProperties;

    public AzureContextAutoConfiguration(AzureProperties azureProperties) {
        Assert.hasText(azureProperties.getCredentialFilePath(),
                "spring.cloud.azure.credentialFilePath must be provided");
        Assert.hasText(azureProperties.getResourceGroup(), "spring.cloud.azure.resourceGroup must be provided");
        Assert.hasText(azureProperties.getRegion(), "spring.cloud.azure.region must be provided");
        this.azureProperties = azureProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public CredentialsProvider credentialsProvider() {
        return new DefaultCredentialsProvider(this.azureProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureAdmin azureAdmin(Azure azure) {
        return new AzureAdmin(azure, azureProperties.getResourceGroup(), azureProperties.getRegion());
    }

    @Bean
    @ConditionalOnMissingBean
    public Azure azure() throws IOException {
        return Azure.authenticate(credentialsProvider().getCredentials()).withDefaultSubscription();
    }
}
