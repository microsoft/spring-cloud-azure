/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
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
public class TopicReceiveController {

    private static final Log LOGGER = LogFactory.getLog(TopicReceiveController.class);
    private static final String INPUT_CHANNEL = "topic.input";
    private static final String TOPIC_NAME = "topic";
    private static final String SUBSCRIPTION_NAME = "group1";

    /** This message receiver binding with {@link ServiceBusTopicInboundChannelAdapter}
     *  via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload) {
        LOGGER.info("Message arrived! Payload: " + new String(payload));
    }

    @Bean
    public ServiceBusTopicInboundChannelAdapter topicMessageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, ServiceBusTopicOperation topicOperation) {
        ServiceBusTopicInboundChannelAdapter adapter = new ServiceBusTopicInboundChannelAdapter(TOPIC_NAME,
                topicOperation, SUBSCRIPTION_NAME);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }
}
