/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubInboundChannelAdapter;
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
public class ReceiveController {

    private static final Log LOGGER = LogFactory.getLog(ReceiveController.class);
    private static final String INPUT_CHANNEL = "input";
    private static final String EVENTHUB_NAME = "eventhub";
    private static final String CONSUMER_GROUP = "$Default";

    /** This message receiver binding with {@link EventHubInboundChannelAdapter}
     *  via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload) {
        LOGGER.info("Message arrived! Payload: " + new String(payload));
    }

    @Bean
    public EventHubInboundChannelAdapter messageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, EventHubOperation eventhubOperation) {
        EventHubInboundChannelAdapter adapter = new EventHubInboundChannelAdapter(EVENTHUB_NAME,
                eventhubOperation, CONSUMER_GROUP);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }
}
