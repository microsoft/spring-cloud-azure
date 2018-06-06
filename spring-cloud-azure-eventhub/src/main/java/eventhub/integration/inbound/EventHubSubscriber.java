/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.inbound;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.util.function.Consumer;

public class EventHubSubscriber implements Subscriber<EventData> {
    private static final Log LOGGER = LogFactory.getLog(EventHubSubscriber.class);
    private final EventProcessorHost host;
    private Checkpointer<EventData> checkpointer;

    public EventHubSubscriber(EventProcessorHost host) {
        Assert.notNull(host, "EventProcessorHost can't be null");
        this.host = host;
    }

    @Override
    public void subscribe(Consumer<Iterable<EventData>> consumer) {

        host.registerEventProcessorFactory(context -> new IEventProcessor() {

            @Override
            public void onOpen(PartitionContext context) throws Exception {
                LOGGER.info(String.format("Partition %s is opening", context.getPartitionId()));
                checkpointer = new EventHubCheckpointer(context);
            }

            @Override
            public void onClose(PartitionContext context, CloseReason reason) throws Exception {
                LOGGER.info(String.format("Partition %s is closing for reason %s", context.getPartitionId(), reason));
                checkpointer = null;
            }

            @Override
            public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
                consumer.accept(events);
            }

            @Override
            public void onError(PartitionContext context, Throwable error) {
                LOGGER.error(String.format("Partition %s onError", context.getPartitionId()), error);
            }
        });
    }

    @Override
    public void unsubscribe() {
        this.host.unregisterEventProcessor().exceptionally((ex) -> {
            LOGGER.error("Failed to unregister event processor", ex);
            return null;
        });
    }

    @Override
    public Checkpointer<EventData> getCheckpointer() {
        return this.checkpointer;
    }
}
