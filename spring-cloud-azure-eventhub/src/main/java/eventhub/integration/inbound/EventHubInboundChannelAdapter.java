/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.inbound;

import com.microsoft.azure.eventhubs.EventData;
import eventhub.core.EventHubOperation;
import eventhub.integration.EventHubHeaders;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class EventHubInboundChannelAdapter extends MessageProducerSupport {

    private final String eventHubName;
    private final EventHubOperation eventHubOperation;
    private final String consumerGroup;
    private CheckpointMode checkpointMode = CheckpointMode.BATCH;
    private ListenerMode listenerMode = ListenerMode.BATCH;
    private Subscriber<EventData> subscriber;
    private MessageConverter messageConverter;
    private Map<String, Object> commonHeaders = new HashMap<>();

    public EventHubInboundChannelAdapter(String eventHubName, EventHubOperation eventHubOperation,
                                         String consumerGroup) {
        this.eventHubName = eventHubName;
        this.eventHubOperation = eventHubOperation;
        this.consumerGroup = consumerGroup;
    }

    @Override
    protected void doStart() {
        super.doStart();

        this.subscriber = this.eventHubOperation.subscribe(this.eventHubName, this.consumerGroup);
        this.subscriber.subscribe(this::receiveMessage);

        if (this.checkpointMode == CheckpointMode.MANUAL) {
            // Send the checkpointer downstream so user decides on when to checkpoint.
            this.commonHeaders.put(EventHubHeaders.CHECKPOINTER, subscriber.getCheckpointer());
        }
    }

    private void receiveMessage(Iterable<EventData> events) {

        if (this.listenerMode == ListenerMode.BATCH) {
            sendMessage(toMessage(events));
        } else /* ListenerMode.RECORD */ {
            StreamSupport.stream(events.spliterator(), false).forEach((e) -> {
                sendMessage(toMessage(e));
                if (this.checkpointMode == checkpointMode.RECORD) {
                    this.subscriber.getCheckpointer().checkpoint(e);
                }
            });
        }

        if (this.checkpointMode == checkpointMode.BATCH) {
            this.subscriber.getCheckpointer().checkpoint();
        }

    }

    private Message<?> toMessage(Object payload) {
        if (this.messageConverter == null) {
            return MessageBuilder.withPayload(payload).copyHeaders(commonHeaders).build();
        }
        return this.messageConverter.toMessage(payload, new MessageHeaders(commonHeaders));
    }

    @Override
    protected void doStop() {
        if (this.subscriber != null) {
            this.subscriber.unsubscribe();
        }

        super.doStop();
    }

    public MessageConverter getMessageConverter() {
        return this.messageConverter;
    }

    /**
     * Sets the {@link MessageConverter} to convert the payload of the incoming message from Event hub.
     * If {@code messageConverter} is null, payload is {@code EventData} or
     * {@code Iterable<EventData>} and returned in that form.
     *
     * @param messageConverter converts the payload of the incoming message
     */
    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
