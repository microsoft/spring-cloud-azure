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

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.spring.cloud.context.core.CredentialsProvider;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureContextAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzurePropertiesConfigured() {
        this.contextRunner
                .withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureProperties.class);
                    assertThat(context.getBean(AzureProperties.class).getCredentialFilePath()).isEqualTo("credential");
                });
    }

    @Test
    public void testWithoutAzureProperties() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureProperties.class);
                    assertThat(context.getBean(AzureProperties.class).getCredentialFilePath()).isNull();
                });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public CredentialsProvider credentialsProvider() {
            return mock(CredentialsProvider.class);
        }

        @Bean
        Azure.Authenticated authenticated() {
            return mock(Azure.Authenticated.class);
        }
    }
}
