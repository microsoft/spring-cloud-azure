/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusQueueProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusQueueChannelProvisioner;
import com.microsoft.azure.spring.integration.core.DefaultMessageHandler;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueMessageChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<ServiceBusQueueConsumerProperties>,
                ExtendedProducerProperties<ServiceBusQueueProducerProperties>, ServiceBusQueueChannelProvisioner>
        implements
        ExtendedPropertiesBinder<MessageChannel, ServiceBusQueueConsumerProperties, ServiceBusQueueProducerProperties> {

    private final ServiceBusQueueOperation serviceBusQueueOperation;

    private ServiceBusQueueExtendedBindingProperties bindingProperties = new ServiceBusQueueExtendedBindingProperties();

    public ServiceBusQueueMessageChannelBinder(String[] headersToEmbed,
            @NonNull ServiceBusQueueChannelProvisioner provisioningProvider,
            @NonNull ServiceBusQueueOperation serviceBusQueueOperation) {
        super(headersToEmbed, provisioningProvider);
        this.serviceBusQueueOperation = serviceBusQueueOperation;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ExtendedProducerProperties<ServiceBusQueueProducerProperties> producerProperties,
            MessageChannel errorChannel) {
        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), this.serviceBusQueueOperation);
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
            ExtendedConsumerProperties<ServiceBusQueueConsumerProperties> properties) {
        CheckpointConfig checkpointConfig =
                CheckpointConfig.builder().checkpointMode(properties.getExtension().getCheckpointMode()).build();
        this.serviceBusQueueOperation.setCheckpointConfig(checkpointConfig);
        ServiceBusQueueInboundChannelAdapter inboundAdapter =
                new ServiceBusQueueInboundChannelAdapter(destination.getName(), this.serviceBusQueueOperation);
        inboundAdapter.setBeanFactory(getBeanFactory());
        return inboundAdapter;
    }

    @Override
    public ServiceBusQueueConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public ServiceBusQueueProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.bindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.bindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setBindingProperties(ServiceBusQueueExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }
}
