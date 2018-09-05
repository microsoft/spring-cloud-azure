/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Warren Zhu
 */
@RestController
public class QueueReceiveController {

    private static final Log LOGGER = LogFactory.getLog(QueueReceiveController.class);
    private static final String INPUT_CHANNEL = "queue.input";
    private static final String QUEUE_NAME = "example";

    /** This message receiver binding with {@link ServiceBusQueueInboundChannelAdapter}
     *  via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload) {
        LOGGER.info("Message arrived! Payload: " + new String(payload));
    }

    @Bean
    public ServiceBusQueueInboundChannelAdapter queueMessageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, ServiceBusQueueOperation queueOperation) {
        ServiceBusQueueInboundChannelAdapter adapter = new ServiceBusQueueInboundChannelAdapter(QUEUE_NAME,
                queueOperation);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }
}
