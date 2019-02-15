/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.microsoft.azure.spring.cloud.config.AzureCloudConfigAutoConfiguration.WATCH_TASK_SCHEDULER_NAME;
import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

public class AzureCloudConfigAutoConfigurationTest {
    private static final TaskScheduler TEST_SCHEDULER = new ThreadPoolTaskScheduler();
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(STORE_NAME_PROP, TEST_STORE_NAME))
            .withConfiguration(AutoConfigurations.of(AzureConfigBootstrapConfiguration.class,
                    AzureCloudConfigAutoConfiguration.class));

    @Test
    public void watchEnabledNotConfiguredShouldNotCreateWatch() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "true")).run(context -> {
            assertThat(context).doesNotHaveBean(AzureCloudConfigWatch.class);
            assertThat(context).doesNotHaveBean(WATCH_TASK_SCHEDULER_NAME);
        });
    }

    @Test
    public void configNotEnabledWatchNotEnabledShouldNotCreateWatch() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "false"),
                propPair(WATCH_ENABLED_PROP, "false")).run(context -> {
           assertThat(context).doesNotHaveBean(AzureCloudConfigWatch.class);
           assertThat(context).doesNotHaveBean(WATCH_TASK_SCHEDULER_NAME);
        });
    }

    @Test
    public void configNotEnabledWatchEnabledShouldNotCreateWatch() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "false"),
                propPair(WATCH_ENABLED_PROP, "true")).run(context -> {
            assertThat(context).doesNotHaveBean(AzureCloudConfigWatch.class);
            assertThat(context).doesNotHaveBean(WATCH_TASK_SCHEDULER_NAME);
        });
    }

    @Test
    public void configEnabledWatchNotEnabledShouldNotCreateWatch() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "true"),
                propPair(WATCH_ENABLED_PROP, "false")).run(context -> {
            assertThat(context).doesNotHaveBean(AzureCloudConfigWatch.class);
            assertThat(context).doesNotHaveBean(WATCH_TASK_SCHEDULER_NAME);
        });
    }

    @Test
    public void configEnabledWatchEnabledShouldCreateWatch() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "true"),
                propPair(WATCH_ENABLED_PROP, "true"),
                propPair(WATCHED_KEY_PROP, TEST_WATCH_KEY)).run(context -> {
            assertThat(context).hasSingleBean(AzureCloudConfigWatch.class);
            assertThat(context).hasBean(WATCH_TASK_SCHEDULER_NAME);
            assertThat(context).hasSingleBean(TaskScheduler.class);
        });
    }

    @Test
    public void taskSchedulerCanBeCustomizedByUser() {
        contextRunner.withPropertyValues(propPair(CONFIG_ENABLED_PROP, "true"),
                propPair(WATCH_ENABLED_PROP, "true"),
                propPair(WATCHED_KEY_PROP, TEST_WATCH_KEY))
                .withUserConfiguration(TestConfiguration.class).run(context -> {
            assertThat(context).hasSingleBean(AzureCloudConfigWatch.class);
            assertThat(context).hasBean(WATCH_TASK_SCHEDULER_NAME);
            assertThat(context).hasSingleBean(TaskScheduler.class);
            assertThat(context.getBean(WATCH_TASK_SCHEDULER_NAME)).isEqualTo(TEST_SCHEDULER);
        });
    }

    @Configuration
    static class TestConfiguration {
        @Bean(name = WATCH_TASK_SCHEDULER_NAME)
        public TaskScheduler getTaskScheduler() {
            return TEST_SCHEDULER;
        }
    }
}
