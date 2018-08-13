/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import com.microsoft.azure.spring.integration.storage.queue.outbound.StorageQueueMessageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;


@SpringBootApplication
public class StorageQueueApplication {

    private static final String DESTINATION = "exampleQueue";
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
        return new StorageQueueMessageHandler(DESTINATION, storageQueueOperation);
    }

    @Bean
    @InboundChannelAdapter(channel = INPUT_CHANNEL, poller = @Poller(fixedDelay = "5000"))
    public StorageQueueMessageSource StorageQueueMessageSource(StorageQueueOperation storageQueueOperation) {
        return new StorageQueueMessageSource(DESTINATION, storageQueueOperation);
    }

    @Bean
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public MessageHandler handler() {
        return message -> LOGGER.info(new String((byte[]) message.getPayload()));
    }


}
