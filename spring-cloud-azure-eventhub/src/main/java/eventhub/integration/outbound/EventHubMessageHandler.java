/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eventhub.integration.outbound;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.eventhubs.EventData;
import eventhub.core.EventHubOperation;
import eventhub.core.EventHubTemplate;
import eventhub.core.PartitionSupplier;
import eventhub.integration.EventHubHeaders;

import org.springframework.integration.codec.CodecMessageConverter;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFutureCallback;

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

    private final EventHubTemplate eventHubTemplate;

    private boolean sync;

    private CodecMessageConverter messageConverter;

    private ListenableFutureCallback<Void> sendCallback;

    public EventHubMessageHandler(EventHubTemplate eventHubTemplate) {
        this.eventHubTemplate = eventHubTemplate;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {

        PartitionSupplier partitionSupplier = toPartitionSupplier(message);

        String eventHubName = message.getHeaders().get(EventHubHeaders.NAME, String.class);

        EventData eventData = toEventData(message);

        if (this.sync) {
            this.eventHubTemplate.send(eventHubName, eventData, partitionSupplier);
        }
        else {
            CompletableFuture future = this.eventHubTemplate.sendAsync(eventHubName, eventData, partitionSupplier);
            future.whenComplete((t, ex) -> {
                if (ex != null) {
                    this.sendCallback.onFailure((Throwable) ex);
                }
                else {
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

    private PartitionSupplier toPartitionSupplier(Message<?> message) {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey(message.getHeaders().get(EventHubHeaders.PARTITION_KEY, String.class));
        partitionSupplier
                .setPartitionId(message.getHeaders().get(EventHubHeaders.PARTITION_ID, Integer.class).toString());
        return partitionSupplier;
    }
}
