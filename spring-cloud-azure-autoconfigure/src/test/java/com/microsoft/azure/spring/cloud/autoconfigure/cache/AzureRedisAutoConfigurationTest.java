/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cache;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.cloud.context.core.CredentialsProvider;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureRedisAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(AzureContextAutoConfiguration.class, AzureRedisAutoConfiguration.class))
                                                                                   .withUserConfiguration(
                                                                                           TestConfiguration.class);

    @Test
    public void testWithoutAzureRedisProperties() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    public void testAzureRedisPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.redis.name=redis").run(context -> {
            assertThat(context).hasSingleBean(AzureRedisProperties.class);
            assertThat(context.getBean(AzureRedisProperties.class).getName()).isEqualTo("redis");
            assertThat(context).hasSingleBean(RedisProperties.class);
            assertThat(context.getBean(RedisProperties.class).getPassword()).isEqualTo("key");
            assertThat(context.getBean(RedisProperties.class).getHost()).isEqualTo("localhost");
            assertThat(context.getBean(RedisProperties.class).getPort()).isEqualTo(6379);
            assertThat(context.getBean(RedisProperties.class).isSsl()).isTrue();
        });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public CredentialsProvider credentialsProvider() {
            return mock(CredentialsProvider.class);
        }

        @Bean
        Azure azure() {
            return mock(Azure.class);
        }

        @Bean
        AzureAdmin azureAdmin() {

            AzureAdmin azureAdmin = mock(AzureAdmin.class);
            RedisCache redisCache = mock(RedisCache.class);
            RedisAccessKeys accessKeys = mock(RedisAccessKeys.class);
            when(accessKeys.primaryKey()).thenReturn("key");
            when(redisCache.hostName()).thenReturn("localhost");
            when(redisCache.nonSslPort()).thenReturn(false);
            when(redisCache.sslPort()).thenReturn(6379);
            when(redisCache.shardCount()).thenReturn(0);
            when(redisCache.getKeys()).thenReturn(accessKeys);
            when(azureAdmin.getOrCreateRedisCache(isA(String.class))).thenReturn(redisCache);
            return azureAdmin;
        }

    }
}
