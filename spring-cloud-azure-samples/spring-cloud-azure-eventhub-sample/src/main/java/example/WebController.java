/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.eventhub.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.eventhub.inbound.EventHubInboundChannelAdapter;
import com.microsoft.azure.spring.integration.eventhub.outbound.EventHubMessageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    private static final Log LOGGER = LogFactory.getLog(EventHubApplication.class);
    private static final String EVENT_HUB_NAME = "example";
    private static final String CONSUMER_GROUP = "$Default";

    @Autowired
    EventHubOperation eventHubOperation;

    @Autowired
    AzureAdmin azureAdmin;

    @Autowired
    AzureEventHubProperties eventHubProperties;

    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.eventHubOperation.sendAsync(EVENT_HUB_NAME, EventData.create(message.getBytes()), null);
        return message;
    }

    @PostConstruct
    public void initEventHub(){
        this.azureAdmin.getOrCreateEventHub(eventHubProperties.getNamespace(), EVENT_HUB_NAME);
        this.eventHubOperation.subscribe(EVENT_HUB_NAME, this::messageReceiver, CONSUMER_GROUP);
    }

    private void messageReceiver(Iterable<EventData> eventData) {
        eventData.forEach(e ->
        LOGGER.info("Message arrived! Payload: " + new String(e.getBytes())));
    }
}
