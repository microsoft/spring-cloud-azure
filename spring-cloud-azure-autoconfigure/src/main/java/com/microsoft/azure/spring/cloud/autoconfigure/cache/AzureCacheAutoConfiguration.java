/*
 *  Copyright 2017-2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cache;

import java.io.IOException;

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

/**
 * An auto-configuration for Spring cache using Azure redis cache
 *
 * @author Warren Zhu
 *
 */
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnProperty("spring.cloud.azure.cache.name")
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
