/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.outbound;

import com.microsoft.azure.eventhubs.EventData;
import eventhub.core.EventHubOperation;
import eventhub.integration.EventHubHeaders;
import org.springframework.integration.codec.CodecMessageConverter;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * Outbound channel adapter to publish messages to Azure Event Hub.
 *
 * <p>
 * It delegates real operation to {@link EventHubOperation}. It also
 * converts the {@link Message} payload into a {@link EventData} accepted by
 * the Event Hub Client Library. It supports synchronous and asynchronous * sending.
 *
 * @author Warren Zhu
 */
public class EventHubMessageHandler extends AbstractMessageHandler {
    private final String eventHubName;
    private final EventHubOperation eventHubTemplate;
    private boolean sync = false;
    private MessageConverter messageConverter;
    private ListenableFutureCallback<Void> sendCallback;

    public EventHubMessageHandler(String eventHubName, EventHubOperation eventHubTemplate) {
        this.eventHubName = eventHubName;
        this.eventHubTemplate = eventHubTemplate;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {

        PartitionSupplier partitionSupplier = toPartitionSupplier(message);

        String eventHubName = toEventHubName(message);

        EventData eventData = toEventData(message);

        CompletableFuture future = this.eventHubTemplate.sendAsync(eventHubName, eventData, partitionSupplier);

        if (this.sync) {
            future.get();
        } else if (sendCallback != null) {
            future.whenComplete((t, ex) -> {
                if (ex != null) {
                    this.sendCallback.onFailure((Throwable) ex);
                } else {
                    this.sendCallback.onSuccess((Void) t);
                }
            });
        }
    }

    public boolean isSync() {
        return this.sync;
    }

    /**
     * Set send method to be synchronous or asynchronous.
     *
     * <p>
     * send is asynchronous be default.
     *
     * @param sync true for synchronous, false for asynchronous
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public void setMessageConverter(CodecMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public void setSendCallback(ListenableFutureCallback<Void> sendCallback) {
        this.sendCallback = sendCallback;
    }

    private EventData toEventData(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof EventData) {
            return (EventData) payload;
        }

        if (payload instanceof String) {
            return EventData.create(((String) payload).getBytes(Charset.defaultCharset()));
        }

        if (payload instanceof byte[]) {
            return EventData.create((byte[]) payload);
        }

        return EventData.create((byte[]) this.messageConverter.fromMessage(message, byte[].class));
    }

    private String toEventHubName(Message<?> message) {
        if (message.getHeaders().containsKey(EventHubHeaders.NAME)) {
            return message.getHeaders().get(EventHubHeaders.NAME, String.class);
        }

        return this.eventHubName;
    }

    private PartitionSupplier toPartitionSupplier(Message<?> message) {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey(message.getHeaders().get(EventHubHeaders.PARTITION_KEY, String.class));
        partitionSupplier
                .setPartitionId(message.getHeaders().get(EventHubHeaders.PARTITION_ID, Integer.class).toString());
        return partitionSupplier;
    }
}
