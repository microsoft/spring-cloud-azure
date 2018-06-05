/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.eventhub.stream.binder;

import com.microsoft.azure.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubExtendedBindingProperties;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import eventhub.core.EventHubOperation;
import eventhub.integration.inbound.CheckpointMode;
import eventhub.integration.inbound.EventHubInboundChannelAdapter;
import eventhub.integration.inbound.ListenerMode;
import eventhub.integration.outbound.EventHubMessageHandler;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class EventHubMessageChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<EventHubConsumerProperties>,
                ExtendedProducerProperties<EventHubProducerProperties>, EventHubChannelProvisioner>
        implements ExtendedPropertiesBinder<MessageChannel, EventHubConsumerProperties, EventHubProducerProperties> {

    private EventHubOperation eventHubOperation;

    private EventHubExtendedBindingProperties bindingProperties = new EventHubExtendedBindingProperties();

    public EventHubMessageChannelBinder(String[] headersToEmbed, EventHubChannelProvisioner provisioningProvider,
            EventHubOperation eventHubOperation) {
        super(headersToEmbed, provisioningProvider);
        this.eventHubOperation = eventHubOperation;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ExtendedProducerProperties<EventHubProducerProperties> producerProperties,
            MessageChannel errorChannel) {
        EventHubMessageHandler handler = new EventHubMessageHandler(destination.getName(), this.eventHubOperation);
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        if (producerProperties.isPartitioned()) {
            handler.setPartitionKeyExpressionString(
                    "'partitionKey-' + headers['" + BinderHeaders.PARTITION_HEADER + "']");
        } else {
            handler
                    .setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }

        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ExtendedConsumerProperties<EventHubConsumerProperties> properties) {
        EventHubInboundChannelAdapter inboundAdapter =
                new EventHubInboundChannelAdapter(destination.getName(), this.eventHubOperation, group);
        // Spring cloud stream only support record mode now
        inboundAdapter.setListenerMode(ListenerMode.RECORD);
        inboundAdapter.setCheckpointMode(CheckpointMode.RECORD);

        return inboundAdapter;
    }

    @Override
    public EventHubConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public EventHubProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindingProperties.getExtendedProducerProperties(channelName);
    }

}
