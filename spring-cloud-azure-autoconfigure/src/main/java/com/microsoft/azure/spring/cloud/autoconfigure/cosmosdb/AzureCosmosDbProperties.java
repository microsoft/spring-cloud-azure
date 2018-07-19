/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.autoconfigure.cosmosdb;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import javax.annotation.PostConstruct;

@Getter
@Setter
@ConfigurationProperties("spring.cloud.azure.cosmosdb")
public class AzureCosmosDbProperties {
    private String accountName;

    @PostConstruct
    public void validate() {
        Assert.hasText(accountName, "spring.cloud.azure.cosmosdb.account-name must be provided");
    }
}
