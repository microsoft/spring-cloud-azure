/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base class of outbound adapter to publish to azure backed messaging service
 *
 * <p>
 * It delegates real operation to {@link SendOperation} and
 * converts the {@link Message} payload into specific type {@link D} accepted by
 * Azure SDK. It supports synchronous and asynchronous sending.
 *
 * @author Warren Zhu
 */
@Getter
@Setter
public abstract class AbstractAzureMessageHandler<D> extends AbstractMessageHandler {
    private static final long DEFAULT_SEND_TIMEOUT = 10000;
    private final String destination;
    private final SendOperation<D> sendOperation;
    protected MessageConverter messageConverter;
    private boolean sync = false;
    private ListenableFutureCallback<Void> sendCallback;
    private EvaluationContext evaluationContext;
    private Expression sendTimeoutExpression = new ValueExpression<>(DEFAULT_SEND_TIMEOUT);
    private Expression partitionKeyExpression;

    public AbstractAzureMessageHandler(String destination, SendOperation<D> sendOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        Assert.notNull(sendOperation, "sendOperation can't be null");
        this.destination = destination;
        this.sendOperation = sendOperation;
    }

    @Override
    protected void onInit() throws Exception {
        super.onInit();
        this.evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {

        PartitionSupplier partitionSupplier = toPartitionSupplier(message);
        String destination = toDestination(message);
        D azureMessage = toAzureMessage(message);
        CompletableFuture future = this.sendOperation.sendAsync(destination, azureMessage, partitionSupplier);

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

    public void setPartitionKey(String partitionKey) {
        setPartitionKeyExpression(new LiteralExpression(partitionKey));
    }

    public void setPartitionKeyExpressionString(String partitionKeyExpression) {
        setPartitionKeyExpression(EXPRESSION_PARSER.parseExpression(partitionKeyExpression));
    }

    public void setPartitionKeyExpression(Expression partitionKeyExpression) {
        this.partitionKeyExpression = partitionKeyExpression;
    }

    public abstract D toAzureMessage(Message<?> message);

    private String toDestination(Message<?> message) {
        if (message.getHeaders().containsKey(AzureHeaders.NAME)) {
            return message.getHeaders().get(AzureHeaders.NAME, String.class);
        }

        return this.destination;
    }

    private PartitionSupplier toPartitionSupplier(Message<?> message) {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        String partitionKey = message.getHeaders().get(AzureHeaders.PARTITION_KEY, String.class);
        if (!StringUtils.hasText(partitionKey) && this.partitionKeyExpression != null) {
            partitionKey = this.partitionKeyExpression.getValue(this.evaluationContext, message, String.class);
        }

        if (StringUtils.hasText(partitionKey)) {
            partitionSupplier.setPartitionKey(partitionKey);
        }

        if (message.getHeaders().containsKey(AzureHeaders.PARTITION_ID)) {
            partitionSupplier
                    .setPartitionId(message.getHeaders().get(AzureHeaders.PARTITION_ID, Integer.class).toString());
        }
        return partitionSupplier;
    }
}
