/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base class of outbound adapter to publish to azure backed messaging service
 *
 * <p>
 * It delegates real operation to {@link SendOperation} which supports synchronous and asynchronous sending.
 *
 * @author Warren Zhu
 */
@Getter
@Setter
@Slf4j
public class DefaultMessageHandler extends AbstractMessageHandler {
    private static final long DEFAULT_SEND_TIMEOUT = 10000;
    private final String destination;
    private final SendOperation sendOperation;
    protected MessageConverter messageConverter;
    private boolean sync = false;
    private ListenableFutureCallback<Void> sendCallback;
    private EvaluationContext evaluationContext;
    private Expression sendTimeoutExpression = new ValueExpression<>(DEFAULT_SEND_TIMEOUT);
    private Expression partitionKeyExpression;

    public DefaultMessageHandler(String destination, @NonNull SendOperation sendOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
        this.sendOperation = sendOperation;
    }

    @Override
    protected void onInit() throws Exception {
        super.onInit();
        this.evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
        log.info("Started DefaultMessageHandler with properties: {}", buildPropertiesMap());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleMessageInternal(Message<?> message) throws Exception {

        PartitionSupplier partitionSupplier = toPartitionSupplier(message);
        String destination = toDestination(message);
        CompletableFuture<?> future = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        if (this.sync) {
            waitingSendResponse(future, message);
            return;
        }

        handleSendResponseAsync(message, future);
    }

    private void handleSendResponseAsync(Message<?> message, CompletableFuture<?> future) {
        future.handle((t, ex) -> {
            if (ex != null) {
                if (log.isWarnEnabled()) {
                    log.warn("{} sent failed in async mode", message);
                }
                if (this.sendCallback != null) {
                    this.sendCallback.onFailure(ex);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("{} sent successfully in async mode", message);
                }
                if (this.sendCallback != null) {
                    this.sendCallback.onSuccess((Void) t);
                }
            }

            return null;
        });
    }

    private void waitingSendResponse(CompletableFuture future, Message<?> message)
            throws InterruptedException, ExecutionException {
        Long sendTimeout = this.sendTimeoutExpression.getValue(this.evaluationContext, message, Long.class);
        if (sendTimeout < 0) {
            future.get();
        } else {
            try {
                future.get(sendTimeout, TimeUnit.MILLISECONDS);
                if (log.isDebugEnabled()) {
                    log.debug("{} sent successfully in sync mode", message);
                }
            } catch (TimeoutException e) {
                throw new MessageTimeoutException(message, "Timeout waiting for send event hub response", e);
            }
        }
    }

    public void setSync(boolean sync) {
        this.sync = sync;
        log.info("DefaultMessageHandler sync becomes: {}", sync);
    }

    public void setSendTimeout(long sendTimeout) {
        setSendTimeoutExpression(new ValueExpression<>(sendTimeout));
    }

    public void setSendTimeoutExpression(Expression sendTimeoutExpression) {
        Assert.notNull(sendTimeoutExpression, "'sendTimeoutExpression' must not be null");
        this.sendTimeoutExpression = sendTimeoutExpression;
        log.info("DefaultMessageHandler syncTimeout becomes: {}", sendTimeoutExpression);
    }

    public void setPartitionKey(String partitionKey) {
        setPartitionKeyExpression(new LiteralExpression(partitionKey));
    }

    public void setPartitionKeyExpressionString(String partitionKeyExpression) {
        setPartitionKeyExpression(EXPRESSION_PARSER.parseExpression(partitionKeyExpression));
    }

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

    private Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sync", sync);
        properties.put("sendTimeout", sendTimeoutExpression);
        properties.put("destination", destination);

        return properties;
    }
}
