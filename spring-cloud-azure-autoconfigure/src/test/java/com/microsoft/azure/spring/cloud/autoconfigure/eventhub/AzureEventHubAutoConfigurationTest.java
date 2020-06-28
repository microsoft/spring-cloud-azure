/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureEventHubAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureEventHubDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testWithoutEventHubClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(EventHubConsumerAsyncClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureEventHubPropertiesNamespaceIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=")
                          .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=")
                          .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=nsl")
                          .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=1")
                          .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Test
    public void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1").
                withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1").run(context -> {
            assertThat(context).hasSingleBean(AzureEventHubProperties.class);
            assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo("ns1");
            assertThat(context.getBean(AzureEventHubProperties.class).getCheckpointStorageAccount()).isEqualTo("sa1");
            assertThat(context).hasSingleBean(EventHubClientFactory.class);
            assertThat(context).hasSingleBean(EventHubOperation.class);
        });
    }

    @Test
    public void testDefaultRetryPolicyIsInPlace() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1").
            withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1").run(context -> {
            AmqpRetryOptions retryOptions = context.getBean(AzureEventHubProperties.class).getConsumerRetryOptions();

            assertThat(retryOptions.getMaxRetries()).isEqualTo(3);
            assertThat(retryOptions.getDelay()).isEqualTo(Duration.ofMillis(800));
            assertThat(retryOptions.getMaxDelay()).isEqualTo(Duration.ofMinutes(1));
            assertThat(retryOptions.getTryTimeout()).isEqualTo(Duration.ofMinutes(1));
            assertThat(retryOptions.getMode()).isEqualTo(AmqpRetryMode.EXPONENTIAL);
        });
    }

    @Test
    public void testConsumerMaxRetriesIsChangeable() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1")
            .withPropertyValues("spring.cloud.azure.eventhub.consumer-max-retries=10")
            .run(context -> {
            AmqpRetryOptions retryOptions = context.getBean(AzureEventHubProperties.class).getConsumerRetryOptions();

            assertThat(retryOptions.getMaxRetries()).isEqualTo(10);
        });
    }

    @Test
    public void testConsumerDelayIsChangeable() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1")
            .withPropertyValues("spring.cloud.azure.eventhub.consumer-delay=1500")
            .run(context -> {
                AmqpRetryOptions retryOptions = context.getBean(AzureEventHubProperties.class).getConsumerRetryOptions();
                assertThat(retryOptions.getDelay()).isEqualTo(Duration.ofMillis(1500));
            });
    }

    @Test
    public void testConsumerMaxDelayIsChangeable() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1")
            .withPropertyValues("spring.cloud.azure.eventhub.consumer-max-delay=1500")
            .run(context -> {
                AmqpRetryOptions retryOptions = context.getBean(AzureEventHubProperties.class).getConsumerRetryOptions();
                assertThat(retryOptions.getMaxDelay()).isEqualTo(Duration.ofSeconds(1500));
            });
    }

    @Test
    public void testConsumerTryTimeoutIsChangeable() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1")
            .withPropertyValues("spring.cloud.azure.eventhub.consumer-try-timeout=100")
            .run(context -> {
                AmqpRetryOptions retryOptions = context.getBean(AzureEventHubProperties.class).getConsumerRetryOptions();
                assertThat(retryOptions.getTryTimeout()).isEqualTo(Duration.ofSeconds(100));
            });
    }

    @Test
    public void testConsumerRetryModeIsChangeable() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1")
            .withPropertyValues("spring.cloud.azure.eventhub.consumer-retry-mode=FIXED")
            .run(context -> {
                AmqpRetryOptions retryOptions = context.getBean(AzureEventHubProperties.class).getConsumerRetryOptions();
                assertThat(retryOptions.getMode()).isEqualTo(AmqpRetryMode.FIXED);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void testRetryModeInvalidValue() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1")
            .withPropertyValues("spring.cloud.azure.eventhub.consumer-retry-mode=INVALID")
            .run(context -> {
                context.getBean(AzureEventHubProperties.class);
            });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        EventHubClientFactory clientFactory() {
            return mock(EventHubClientFactory.class);
        }

        @Bean
        EventHubConnectionStringProvider connectionStringProvider() {
            return mock(EventHubConnectionStringProvider.class);
        }
    }
}
