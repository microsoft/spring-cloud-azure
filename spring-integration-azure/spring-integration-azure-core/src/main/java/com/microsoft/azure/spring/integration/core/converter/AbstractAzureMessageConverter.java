/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class handles common conversion logic between <T> and {@link Message}
 *
 * @author Warren Zhu
 */
public abstract class AbstractAzureMessageConverter<T> implements AzureMessageConverter<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractAzureMessageConverter.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    protected static byte[] toPayload(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to write JSON: " + object, e);
        }
    }

    private static <U> U fromPayload(byte[] payload, Class<U> payloadType) {
        try {
            return objectMapper.readerFor(payloadType).readValue(payload);
        } catch (IOException e) {
            throw new ConversionException("Failed to read JSON: " + Arrays.toString(payload), e);
        }
    }

    @Override
    public T fromMessage(@NonNull Message<?> message, @NonNull Class<T> targetClass) {
        T azureMessage = internalFromMessage(message, targetClass);

        setCustomHeaders(message.getHeaders(), azureMessage);

        return azureMessage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Message<U> toMessage(@NonNull T azureMessage, Map<String, Object> headers,
                                    @NonNull Class<U> targetPayloadClass) {
        Map<String, Object> mergedHeaders = new HashMap<>();
        mergedHeaders.putAll(buildCustomHeaders(azureMessage));
        mergedHeaders.putAll(headers);
        return (Message<U>) internalToMessage(azureMessage, mergedHeaders, targetPayloadClass);
    }

    protected abstract byte[] getPayload(T azureMessage);

    protected abstract T fromString(String payload);

    protected abstract T fromByte(byte[] payload);

    protected void setCustomHeaders(MessageHeaders headers, T azureMessage) {
    }

    protected Map<String, Object> buildCustomHeaders(T azureMessage) {
        return new HashMap<>();
    }

    private T internalFromMessage(Message<?> message, Class<T> targetClass) {
        Object payload = message.getPayload();

        if (targetClass.isInstance(payload)) {
            return targetClass.cast(payload);
        }

        if (payload instanceof String) {
            return fromString((String) payload);
        }

        if (payload instanceof byte[]) {
            return fromByte((byte[]) payload);
        }

        return fromByte(toPayload(payload));
    }

    private <U> Message<?> internalToMessage(T azureMessage, Map<String, Object> headers, Class<U> targetPayloadClass) {
        byte[] payload = getPayload(azureMessage);

        if (targetPayloadClass.isInstance(azureMessage)) {
            return MessageBuilder.withPayload(azureMessage).copyHeaders(headers).build();
        }

        if (targetPayloadClass == String.class) {
            return MessageBuilder.withPayload(new String(payload)).copyHeaders(headers).build();
        }

        if (targetPayloadClass == byte[].class) {
            return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
        }

        return MessageBuilder.withPayload(fromPayload(payload, targetPayloadClass)).copyHeaders(headers).build();
    }

    /**
     * Convert the json string to class targetType instance.
     * @param value json string
     * @param targetType target class to convert
     * @param <M> Target class type
     * @return Return the corresponding class instance
     */
    protected <M> M readValue(String value, Class<M> targetType) {
        try {
            return objectMapper.readValue(value, targetType);
        } catch (IOException e) {
            throw new ConversionException("Failed to read JSON: " + value, e);
        }
    }

    /**
     * Check value is valid json string.
     * @param value json string to check
     * @return true if it's json string.
     */
    protected boolean isValidJson(Object value) {
        try {
            if (value instanceof String) {
                objectMapper.readTree((String) value);
                return true;
            }
            log.warn("Not a valid json string: " + value);
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Convert the object to json string
     * @param value object to be converted
     * @return json string
     */
    protected String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert to JSON: " + value.toString(), e);
        }
    }
}
