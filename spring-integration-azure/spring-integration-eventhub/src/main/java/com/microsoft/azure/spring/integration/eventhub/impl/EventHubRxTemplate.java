/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.impl;

import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubRxOperation;
import org.springframework.messaging.Message;
import rx.Observable;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link EventHubRxOperation}.
 *
 * @author Warren Zhu
 */
public class EventHubRxTemplate extends AbstractEventHubTemplate implements EventHubRxOperation {

    private final ConcurrentHashMap<Tuple<String, String>, Observable<Message<?>>> subjectByNameAndGroup =
            new ConcurrentHashMap<>();

    public EventHubRxTemplate(EventHubClientFactory clientFactory) {
        super(clientFactory);
    }

    private static <T> Observable<T> toObservable(CompletableFuture<T> future) {
        return Observable.create(subscriber -> future.whenComplete((result, error) -> {
            if (error != null) {
                subscriber.onError(error);
            } else {
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        }));
    }

    @Override
    public <T> Observable<Void> sendRx(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        return toObservable(sendAsync(destination, message, partitionSupplier));
    }

    @Override
    public Observable<Message<?>> subscribe(String destination, String consumerGroup, Class<?> messagePayloadType) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        subjectByNameAndGroup.computeIfAbsent(nameAndConsumerGroup, k -> Observable.<Message<?>>create(subscriber -> {
            this.register(destination, consumerGroup,
                    new EventHubProcessor(subscriber::onNext, messagePayloadType, getCheckpointConfig(),
                            getMessageConverter()));
            subscriber.add(Subscriptions.create(() -> unregister(destination, consumerGroup)));
        }).share());

        return subjectByNameAndGroup.get(nameAndConsumerGroup);
    }

}
