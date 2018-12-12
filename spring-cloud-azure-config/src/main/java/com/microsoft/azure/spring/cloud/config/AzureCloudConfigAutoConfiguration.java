/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ConditionalOnClass(RefreshEndpoint.class)
@ConditionalOnProperty(name = AzureCloudConfigProperties.CONFIG_PREFIX + ".watch.enabled", matchIfMissing = true)
public class AzureCloudConfigAutoConfiguration {
    public static final String WATCH_TASK_SCHEDULER_NAME = "azureConfigWatchTaskScheduler";

    @Bean
    public AzureCloudConfigWatch getConfigWatch(ConfigServiceOperations operations,
                                                AzureCloudConfigProperties properties,
                                                @Qualifier(WATCH_TASK_SCHEDULER_NAME) TaskScheduler scheduler) {
        return new AzureCloudConfigWatch(operations, properties, scheduler);
    }

    @Bean(name = WATCH_TASK_SCHEDULER_NAME)
    @ConditionalOnMissingBean
    public TaskScheduler getTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
}
