/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

/**
 * @author Warren Zhu
 */
public enum DatabaseType {
    SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:1433;database=%s;" +
            "encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");

    private final String jdbcDriverName;

    private final String jdbcUrlTemplate;

    DatabaseType(String jdbcDriverName, String jdbcUrlTemplate) {
        this.jdbcDriverName = jdbcDriverName;
        this.jdbcUrlTemplate = jdbcUrlTemplate;
    }

    public String getJdbcDriverName() {
        return this.jdbcDriverName;
    }

    public String getJdbcUrlTemplate() {
        return this.jdbcUrlTemplate;
    }

}
