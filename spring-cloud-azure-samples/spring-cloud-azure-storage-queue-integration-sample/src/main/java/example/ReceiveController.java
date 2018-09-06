package example;

import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Miao Cao
 */
@RestController
public class ReceiveController {
    /*Storage queue name can only be made up of lowercase letters, the numbers and the hyphen(-).*/
    private static final String STORAGE_QUEUE_NAME = "example";
    private static final String INPUT_CHANNEL = "inputChannel";
    private static final Log LOGGER = LogFactory.getLog(SendController.class);


    @Bean
    @InboundChannelAdapter(channel = INPUT_CHANNEL, poller = @Poller(fixedDelay = "5000"))
    public StorageQueueMessageSource StorageQueueMessageSource(StorageQueueOperation storageQueueOperation) {
        storageQueueOperation.setVisibilityTimeoutInSeconds(10);
        storageQueueOperation.setCheckpointMode(CheckpointMode.RECORD);
        storageQueueOperation.setMessagePayloadType(String.class);
        StorageQueueMessageSource messageSource =
                new StorageQueueMessageSource(STORAGE_QUEUE_NAME, storageQueueOperation);
        return messageSource;
    }

    /** This message receiver binding with {@link StorageQueueMessageSource}
     *  via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(Message<?> message) {
        LOGGER.info("message received: " + message.getPayload());
    }
}
