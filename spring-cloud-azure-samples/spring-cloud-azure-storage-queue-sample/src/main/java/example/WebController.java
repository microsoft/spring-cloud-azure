/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @author Miao Cao
 */
@RestController
public class WebController {

    private static final Log LOGGER = LogFactory.getLog(WebController.class);
    private static final String STORAGE_QUEUE_NAME = "example";

    @Autowired
    StorageQueueOperation storageQueueOperation;

    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.storageQueueOperation.sendAsync(STORAGE_QUEUE_NAME, MessageBuilder.withPayload(message).build());
        return message;
    }

    @GetMapping("/messages")
    public String receive() throws ExecutionException, InterruptedException {
        this.storageQueueOperation.setMessagePayloadType(String.class);
        Message<?> message = this.storageQueueOperation.receiveAsync(STORAGE_QUEUE_NAME).get();
        if(message == null) {
            LOGGER.info("You have no new messages.");
            return null;
        }
        LOGGER.info("Message arrived! Payload: " + message.getPayload());
        return (String) message.getPayload();
    }
}
