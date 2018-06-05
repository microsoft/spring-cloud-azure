/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.outbound;

import com.microsoft.azure.eventhubs.EventData;
import eventhub.core.EventHubOperation;
import eventhub.integration.EventHubHeaders;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.integration.codec.CodecMessageConverter;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    private static final long DEFAULT_SEND_TIMEOUT = 10000;

    private final String eventHubName;
    private final EventHubOperation eventHubOperation;
    private boolean sync = false;
    private MessageConverter messageConverter;
    private ListenableFutureCallback<Void> sendCallback;
    private EvaluationContext evaluationContext;
    private Expression sendTimeoutExpression = new ValueExpression<>(DEFAULT_SEND_TIMEOUT);

    public EventHubMessageHandler(String eventHubName, EventHubOperation eventHubOperation) {
        Assert.hasText(eventHubName, "eventHubName can't be null or empty");
        Assert.notNull(eventHubOperation, "eventHubOperation can't be null");
        this.eventHubName = eventHubName;
        this.eventHubOperation = eventHubOperation;
    }

    @Override
    protected void onInit() throws Exception {
        super.onInit();
        this.evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {

        PartitionSupplier partitionSupplier = toPartitionSupplier(message);
        String eventHubName = toEventHubName(message);
        EventData eventData = toEventData(message);
        CompletableFuture future = this.eventHubOperation.sendAsync(eventHubName, eventData, partitionSupplier);

        if (this.sync) {
            Long sendTimeout = this.sendTimeoutExpression.getValue(this.evaluationContext, message, Long.class);
            if (sendTimeout == null || sendTimeout < 0) {
                future.get();
            } else {
                future.get(sendTimeout, TimeUnit.MILLISECONDS);
            }
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

    public void setSendTimeout(long sendTimeout) {
        setSendTimeoutExpression(new ValueExpression<>(sendTimeout));
    }

    public void setSendTimeoutExpressionString(String sendTimeoutExpression) {
        setSendTimeoutExpression(EXPRESSION_PARSER.parseExpression(sendTimeoutExpression));
    }

    public void setSendTimeoutExpression(Expression sendTimeoutExpression) {
        Assert.notNull(sendTimeoutExpression, "'sendTimeoutExpression' must not be null");
        this.sendTimeoutExpression = sendTimeoutExpression;
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
        if (message.getHeaders().containsKey(EventHubHeaders.PARTITION_KEY)) {
            partitionSupplier.setPartitionKey(message.getHeaders().get(EventHubHeaders.PARTITION_KEY, String.class));
        }

        if (message.getHeaders().containsKey(EventHubHeaders.PARTITION_ID)) {
            partitionSupplier
                    .setPartitionId(message.getHeaders().get(EventHubHeaders.PARTITION_ID, Integer.class).toString());
        }
        return partitionSupplier;
    }
}
