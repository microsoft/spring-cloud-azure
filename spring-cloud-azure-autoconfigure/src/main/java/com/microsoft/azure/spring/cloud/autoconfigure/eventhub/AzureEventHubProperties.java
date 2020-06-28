/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Pattern;
import java.time.Duration;

/**
 * @author Warren Zhu
 */
@Validated
@ConfigurationProperties("spring.cloud.azure.eventhub")
public class AzureEventHubProperties {

    private String namespace;

    private String connectionString;

    @Pattern(regexp = "^[a-z0-9]{3,24}$",
            message = "must be between 3 and 24 characters in length and use numbers and lower-case letters only")
    private String checkpointStorageAccount;

    private String checkpointAccessKey;

    private String checkpointContainer;

    // AQMP default retry option in seconds.
    private int consumerTryTimeout = 60;

    // AQMP default retry option in milli seconds.
    private int consumerDelay = 800;

    // AQMP default retry option in seconds.
    private int consumerMaxDelay = 60;

    // AQMP default retry option.
    private String consumerRetryMode = "EXPONENTIAL";

    // AQMP default retry option.
    private int consumerMaxRetries = 3;

    public int getConsumerTryTimeout() {
        return consumerTryTimeout;
    }

    public void setConsumerTryTimeout(int consumerTryTimeout) {
        this.consumerTryTimeout = consumerTryTimeout;
    }

    public int getConsumerDelay() {
        return consumerDelay;
    }

    public void setConsumerDelay(int consumerDelay) {
        this.consumerDelay = consumerDelay;
    }

    public int getConsumerMaxDelay() {
        return consumerMaxDelay;
    }

    public void setConsumerMaxDelay(int consumerMaxDelay) {
        this.consumerMaxDelay = consumerMaxDelay;
    }

    public String getConsumerRetryMode() {
        return consumerRetryMode;
    }

    public void setConsumerRetryMode(String consumerRetryMode) {
        this.consumerRetryMode = consumerRetryMode;
    }

    public int getConsumerMaxRetries() {
        return consumerMaxRetries;
    }

    public void setConsumerMaxRetries(int consumerMaxRetries) {
        this.consumerMaxRetries = consumerMaxRetries;
    }

    public AmqpRetryOptions getConsumerRetryOptions() {
        AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions();

        amqpRetryOptions.setDelay(Duration.ofMillis(consumerDelay));
        amqpRetryOptions.setMaxDelay(Duration.ofSeconds(consumerMaxDelay));
        amqpRetryOptions.setMaxRetries(consumerMaxRetries);
        amqpRetryOptions.setTryTimeout(Duration.ofSeconds(consumerTryTimeout));
        amqpRetryOptions.setMode(AmqpRetryMode.valueOf(this.consumerRetryMode));

        return amqpRetryOptions;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getCheckpointAccessKey() {
        return checkpointAccessKey;
    }

    public void setCheckpointAccessKey(String checkpointAccessKey) {
        this.checkpointAccessKey = checkpointAccessKey;
    }

    public String getCheckpointContainer() {
        return checkpointContainer;
    }

    public void setCheckpointContainer(String checkpointContainer) {
        this.checkpointContainer = checkpointContainer;
    }

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(namespace) && !StringUtils.hasText(connectionString)) {
            throw new IllegalArgumentException("Either 'spring.cloud.azure.eventhub.namespace' or " +
                    "'spring.cloud.azure.eventhub.connection-string' should be provided");
        }
    }
}
