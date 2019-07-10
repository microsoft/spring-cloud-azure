/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

@Configuration
@ConditionalOnClass(JmsConnectionFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class ServiceBusJMSAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusProperties serviceBusProperties) {
        String connectionString = serviceBusProperties.getConnectionString();
        String clientId = serviceBusProperties.getNamespace();

        ConnectionStringResolver csr = new ConnectionStringResolver(connectionString);
        String host = csr.getHost();
        String sasKeyName = csr.getSasKeyName();
        String sasKey = csr.getSasKey();

        String remoteUri = String.format("amqps://%s?amqp.idleTimeout=3600000", host);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(remoteUri);
        jmsConnectionFactory.setRemoteURI(remoteUri);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setUsername(sasKeyName);
        jmsConnectionFactory.setPassword(sasKey);
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) {
        JmsTemplate returnValue = new JmsTemplate();
        returnValue.setConnectionFactory(jmsConnectionFactory);
        return returnValue;
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory returnValue = new DefaultJmsListenerContainerFactory();
        returnValue.setConnectionFactory(connectionFactory);
        return returnValue;
    }
}