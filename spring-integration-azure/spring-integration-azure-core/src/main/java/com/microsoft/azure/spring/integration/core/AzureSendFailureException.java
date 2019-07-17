package com.microsoft.azure.spring.integration.core;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * An exception that is the payload of an {@code ErrorMessage} when a send fails.
 *
 * @author Jacob Severson
 *
 * @since 1.1
 */
public class AzureSendFailureException extends MessagingException {

    public AzureSendFailureException(Message<?> message, Throwable cause) {
        super(message, cause);
    }
}
