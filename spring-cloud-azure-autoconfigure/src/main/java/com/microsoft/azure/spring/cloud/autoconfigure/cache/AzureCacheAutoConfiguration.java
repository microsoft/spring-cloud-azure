/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cache;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

/**
 * An auto-configuration for Spring cache using Azure redis cache
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnProperty("spring.cloud.azure.redis.name")
@EnableConfigurationProperties(AzureCacheProperties.class)
public class AzureCacheAutoConfiguration {

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public RedisProperties redisProperties(Azure.Authenticated authenticated, AzureProperties azureProperties,
                                           AzureCacheProperties cacheProperties) throws IOException {
        String cacheName = cacheProperties.getName();

        RedisCache redisCache = authenticated.withDefaultSubscription().redisCaches()
                .getByResourceGroup(azureProperties.getResourceGroup(), cacheName);

        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setHost(redisCache.hostName());
        redisProperties.setPassword(redisCache.getKeys().primaryKey());

        boolean useSsl = !redisCache.nonSslPort();

        redisProperties.setPort(useSsl ? redisCache.sslPort() : redisCache.port());
        redisProperties.setSsl(useSsl);

        // TODO: handle cluster related config, Azure redis management api unsupported

        return redisProperties;
    }
}
