/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.AzureMessageHandler;
import com.microsoft.azure.spring.integration.core.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.inbound.CheckpointMode;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;


@SpringBootApplication
public class StorageQueueApplication {

    /*Storage queue name can only be made up of lowercase letters, the numbers and the hyphen(-).*/
    private static final String STORAGE_QUEUE_NAME = "example";
    private static final String OUTPUT_CHANNEL = "outputChannel";
    private static final String INPUT_CHANNEL = "inputChannel";
    private static final Log LOGGER = LogFactory.getLog(StorageQueueApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StorageQueueApplication.class, args);
    }

    @MessagingGateway(defaultRequestChannel = OUTPUT_CHANNEL)
    public interface StorageQueueOutboundGateway {
        void send(String text);
    }

    @Bean
    @ServiceActivator(inputChannel = OUTPUT_CHANNEL)
    public MessageHandler messageSender(StorageQueueOperation storageQueueOperation) {
        AzureMessageHandler handler = new AzureMessageHandler(STORAGE_QUEUE_NAME, storageQueueOperation);
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

    @Bean
    @InboundChannelAdapter(channel = INPUT_CHANNEL, poller = @Poller(fixedDelay = "5000"))
    public StorageQueueMessageSource StorageQueueMessageSource(StorageQueueOperation storageQueueOperation) {
        storageQueueOperation.setVisibilityTimeoutInSeconds(10);
        storageQueueOperation.setCheckpointMode(CheckpointMode.MANUAL);
        storageQueueOperation.setMessagePayloadType(String.class);
        StorageQueueMessageSource messageSource =
                new StorageQueueMessageSource(STORAGE_QUEUE_NAME, storageQueueOperation);
        return messageSource;
    }

    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(Message<?> message) {
        LOGGER.info("message received: " + message.getPayload());
        if(message.getPayload().equals("checkpoint me!")) {
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(AzureHeaders.CHECKPOINTER);
            checkpointer.success();
        }
    }
}
