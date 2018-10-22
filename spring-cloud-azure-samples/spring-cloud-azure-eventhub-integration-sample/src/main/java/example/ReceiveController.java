/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubInboundChannelAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Warren Zhu
 */
@RestController
public class ReceiveController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveController.class);
    private static final String INPUT_CHANNEL = "input";
    private static final String EVENTHUB_NAME = "eventhub";
    private static final String CONSUMER_GROUP = "$Default";

    /** This message receiver binding with {@link EventHubInboundChannelAdapter}
     *  via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        String message = new String(payload);
        LOGGER.info("Message arrived! Payload: {}", message);
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                LOGGER.info("Message '{}' successfully checkpointed", message);
            }
            return null;
        });
    }

    @Bean
    public EventHubInboundChannelAdapter messageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, EventHubOperation eventhubOperation) {
        eventhubOperation.setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        EventHubInboundChannelAdapter adapter = new EventHubInboundChannelAdapter(EVENTHUB_NAME,
                eventhubOperation, CONSUMER_GROUP);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }
}
