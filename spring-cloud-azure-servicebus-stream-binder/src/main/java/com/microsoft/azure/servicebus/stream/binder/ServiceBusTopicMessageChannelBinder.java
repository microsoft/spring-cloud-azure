/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusTopicChannelProvisioner;
import com.microsoft.azure.spring.integration.core.AzureMessageHandler;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.eventhub.inbound.ListenerMode;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicMessageChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>, ServiceBusTopicChannelProvisioner>
        implements
        ExtendedPropertiesBinder<MessageChannel, ServiceBusConsumerProperties, ServiceBusProducerProperties> {

    private final ServiceBusTopicOperation serviceBusTopicOperation;

    private ServiceBusExtendedBindingProperties bindingProperties = new ServiceBusExtendedBindingProperties();

    public ServiceBusTopicMessageChannelBinder(String[] headersToEmbed,
            @NonNull ServiceBusTopicChannelProvisioner provisioningProvider, @NonNull ServiceBusTopicOperation
            serviceBusTopicOperation) {
        super(headersToEmbed, provisioningProvider);
        this.serviceBusTopicOperation = serviceBusTopicOperation;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties, MessageChannel errorChannel) {
        AzureMessageHandler handler =
                new AzureMessageHandler(destination.getName(), this.serviceBusTopicOperation);
        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        if (producerProperties.isPartitioned()) {
            handler.setPartitionKeyExpressionString(
                    "'partitionKey-' + headers['" + BinderHeaders.PARTITION_HEADER + "']");
        } else {
            handler.setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }

        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID().toString();
        }
        ServiceBusTopicInboundChannelAdapter inboundAdapter =
                new ServiceBusTopicInboundChannelAdapter(destination.getName(), this.serviceBusTopicOperation, group);
        inboundAdapter.setBeanFactory(getBeanFactory());
        // Spring cloud stream only support record mode now
        inboundAdapter.setListenerMode(ListenerMode.RECORD);
        inboundAdapter.setCheckpointMode(CheckpointMode.BATCH);
        return inboundAdapter;
    }

    @Override
    public ServiceBusConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public ServiceBusProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindingProperties.getExtendedProducerProperties(channelName);
    }

}
