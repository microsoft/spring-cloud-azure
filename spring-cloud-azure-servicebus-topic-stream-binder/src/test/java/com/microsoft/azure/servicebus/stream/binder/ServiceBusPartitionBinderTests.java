/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.support.ServiceBusTopicTestOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test cases are defined in super class
 *
 * @author Warren Zhu
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceBusPartitionBinderTests extends
        PartitionCapableBinderTests<ServiceBusTopicTestBinder, ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>> {
    @Mock
    ServiceBusTopicClientFactory clientFactory;

    @Mock
    SubscriptionClient subscriptionClient;

    private ServiceBusTopicTestBinder binder;

    @BeforeClass
    public static void enableTests() {
    }

    @Before
    public void setUp() {
        when(this.clientFactory.getOrCreateSubscriptionClient(anyString(), anyString()))
                .thenReturn(this.subscriptionClient);
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.subscriptionClient.completeAsync(any())).thenReturn(future);
        this.binder = new ServiceBusTopicTestBinder(new ServiceBusTopicTestOperation(this.clientFactory));
    }

    @Override
    protected boolean usesExplicitRouting() {
        return false;
    }

    @Override
    protected String getClassUnderTestName() {
        return ServiceBusTopicTestBinder.class.getSimpleName();
    }

    @Override
    protected ServiceBusTopicTestBinder getBinder() throws Exception {
        return this.binder;
    }

    @Override
    protected ExtendedConsumerProperties<ServiceBusConsumerProperties> createConsumerProperties() {
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties =
                new ExtendedConsumerProperties<>(new ServiceBusConsumerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

    @Override
    protected ExtendedProducerProperties<ServiceBusProducerProperties> createProducerProperties() {
        ExtendedProducerProperties<ServiceBusProducerProperties> properties =
                new ExtendedProducerProperties<>(new ServiceBusProducerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

    @Override
    public Spy spyOn(String name) {
        return null;
    }

    @Override
    public void testClean() throws Exception {
        // No-op
    }

    @Override
    public void testOneRequiredGroup() {
        // Required group test rely on unimplemented start position of consumer properties
    }

    @Override
    public void testTwoRequiredGroups() {
        // Required group test rely on unimplemented start position of consumer properties
    }

    @Override
    public void testPartitionedModuleJava() {
        // Partitioned consumer mode unsupported yet
        // since event hub processor can't only process certain partition
    }

    @Override
    public void testPartitionedModuleSpEL() {
        // Partitioned consumer mode unsupported
        // since event hub processor can't only process certain partition
    }

    // Same logic as super.testSendAndReceiveNoOriginalContentType() except one line commented below
    @Override
    @SuppressWarnings("rawtypes")
    public void testSendAndReceiveNoOriginalContentType() throws Exception {
        Binder binder = getBinder();

        BindingProperties producerBindingProperties = createProducerBindingProperties(createProducerProperties());
        DirectChannel moduleOutputChannel = createBindableChannel("output", producerBindingProperties);
        BindingProperties inputBindingProperties = createConsumerBindingProperties(createConsumerProperties());
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);
        Binding<MessageChannel> producerBinding =
                binder.bindProducer(String.format("bar%s0", getDestinationNameDelimiter()), moduleOutputChannel,
                        producerBindingProperties.getProducer());
        Binding<MessageChannel> consumerBinding =
                binder.bindConsumer(String.format("bar%s0", getDestinationNameDelimiter()),
                        "testSendAndReceiveNoOriginalContentType", moduleInputChannel, createConsumerProperties());
        binderBindUnbindLatency();

        Message<?> message =
                MessageBuilder.withPayload("foo").setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
                              .build();

        // Comment line below since service bus topic operation is event driven mode
        // but subscriber is not ready in the downstream
        // moduleOutputChannel.send(message);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message<byte[]>> inboundMessageRef = new AtomicReference<Message<byte[]>>();
        moduleInputChannel.subscribe(message1 -> {
            try {
                inboundMessageRef.set((Message<byte[]>) message1);
            } finally {
                latch.countDown();
            }
        });

        moduleOutputChannel.send(message);
        Assert.isTrue(latch.await(5, TimeUnit.SECONDS), "Failed to receive message");
        assertThat(inboundMessageRef.get()).isNotNull();
        assertThat(new String(inboundMessageRef.get().getPayload(), StandardCharsets.UTF_8)).isEqualTo("foo");
        assertThat(inboundMessageRef.get().getHeaders().get(MessageHeaders.CONTENT_TYPE).toString())
                .isEqualTo(MimeTypeUtils.TEXT_PLAIN_VALUE);
        producerBinding.unbind();
        consumerBinding.unbind();
    }
}
