/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.core.AzureMessageHandler;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Spring Integration Channel Adapters for Azure Service Bus code sample.
 *
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableIntegration
public class ServiceBusQueueApplication {

    private static final Log LOGGER = LogFactory.getLog(ServiceBusQueueApplication.class);
    private static final String OUTPUT_CHANNEL = "output";
    private static final String INPUT_CHANNEL = "input";
    private static final String SERVICE_BUS_QUEUE_NAME = "example";

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusQueueApplication.class, args);
    }

    @Bean
    @ServiceActivator(inputChannel = OUTPUT_CHANNEL)
    public MessageHandler messageSender(ServiceBusQueueOperation queueOperation) {
        AzureMessageHandler handler = new AzureMessageHandler(SERVICE_BUS_QUEUE_NAME, queueOperation);
        handler.setSendCallback(new ListenableFutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOGGER.info("Message was sent successfully.");
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.info("There was an error sending the message.");
            }
        });

        return handler;
    }

    @MessagingGateway(defaultRequestChannel = OUTPUT_CHANNEL)
    public interface ServiceBusOutboundGateway {

        void sendToServiceBusQueue(String text);
    }

    @Bean
    public ServiceBusQueueInboundChannelAdapter messageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, ServiceBusQueueOperation queueOperation) {
        ServiceBusQueueInboundChannelAdapter adapter = new ServiceBusQueueInboundChannelAdapter(SERVICE_BUS_QUEUE_NAME,
                queueOperation);
        adapter.setOutputChannel(inputChannel);
        adapter.setCheckpointMode(CheckpointMode.MANUAL);
        return adapter;
    }

    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload) {
        LOGGER.info("Message arrived! Payload: " + new String(payload));
    }
}
