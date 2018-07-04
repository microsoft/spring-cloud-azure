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
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.annotation.PostConstruct;
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
@EnableConfigurationProperties(AzureRedisProperties.class)
public class AzureRedisAutoConfiguration {

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryUtils.telemetryTriggerEvent(telemetryTracker, getClass().getSimpleName());
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public RedisProperties redisProperties(Azure azure, AzureProperties azureProperties,
                                           AzureRedisProperties azureRedisProperties) throws IOException {
        String cacheName = azureRedisProperties.getName();

        RedisCache redisCache = azure.redisCaches()
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
