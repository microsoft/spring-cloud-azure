/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Azure SQL properties.
 *
 * @author Warren Zhu
 */
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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
