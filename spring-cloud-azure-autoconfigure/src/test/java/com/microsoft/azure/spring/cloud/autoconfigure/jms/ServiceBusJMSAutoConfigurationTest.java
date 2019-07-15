/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBusJMSAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ServiceBusJMSAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.jms.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
    }

    @Test
    public void testWithoutServiceBusJMSNamespace() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(JmsConnectionFactory.class))
                .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureServiceBusJMSPropertiesValidation() {
        this.contextRunner.run(context -> context.getBean(AzureServiceBusJMSProperties.class));
    }

//    @Test
//    public void testAzureServiceBusJMSPropertiesConfigured() {
//        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.jms.connection-string=cs1;" +
//                        "spring.cloud.azure.servicebus.jms.client-id=cid1;" +
//                        "spring.cloud.azure.servicebus.jms.idle-timeout=it1").run(context -> {
//            assertThat(context).hasSingleBean(AzureServiceBusJMSProperties.class);
//            assertThat(context.getBean(AzureServiceBusJMSProperties.class).getConnectionString()).isEqualTo("cs1");
//            assertThat(context.getBean(AzureServiceBusJMSProperties.class).getClientId()).isEqualTo("cid1");
//            assertThat(context.getBean(AzureServiceBusJMSProperties.class).getIdleTimeout()).isEqualTo("it1");
//        });
//    }

}
