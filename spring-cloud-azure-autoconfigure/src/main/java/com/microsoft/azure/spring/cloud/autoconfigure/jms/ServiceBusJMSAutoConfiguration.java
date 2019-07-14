/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

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
import java.util.Hashtable;

@Configuration
@ConditionalOnClass(JmsConnectionFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.jms.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusJMSProperties.class)
public class ServiceBusJMSAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusJMSProperties serviceBusJMSProperties) {
        String connectionString = serviceBusJMSProperties.getConnectionString();
        String clientId = serviceBusJMSProperties.getClientId();
        String idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        ConnectionStringResolver csr = new ConnectionStringResolver(connectionString);
        Hashtable hashtable = csr.getResolvedKeysAndValues();
        String host = (String) hashtable.get("host");
        String sasKeyName = (String) hashtable.get("SharedAccessKeyName");
        String sasKey = (String) hashtable.get("SharedAccessKey");

        String remoteUri = String.format("amqps://%s?amqp.idleTimeout=%s", host, idleTimeout);
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
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(jmsConnectionFactory);
        return jmsTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        jmsListenerContainerFactory.setConnectionFactory(connectionFactory);
        return jmsListenerContainerFactory;
    }
}
