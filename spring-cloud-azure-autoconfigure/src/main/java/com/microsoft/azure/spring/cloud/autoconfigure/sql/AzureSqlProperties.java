/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * Azure SQL properties.
 *
 * @author Warren Zhu
 */
@Getter
@Setter
@ConfigurationProperties("spring.cloud.azure.sql")
public class AzureSqlProperties {

    /**
     * Name of the database in the Azure SQL instance.
     */
    private String databaseName;

    /**
     * Name of the database server in the Azure SQL instance.
     */
    private String serverName;

    @PostConstruct
    public void validate() {
        Assert.hasText(serverName, "spring.cloud.azure.sql.serverName must be provided");
        Assert.hasText(databaseName, "spring.cloud.azure.sql.databaseName must be provided");
    }
}
