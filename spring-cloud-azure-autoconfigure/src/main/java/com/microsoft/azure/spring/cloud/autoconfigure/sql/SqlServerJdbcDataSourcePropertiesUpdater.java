/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * @author Warren Zhu
 */
public class SqlServerJdbcDataSourcePropertiesUpdater extends AbstractJdbcDatasourcePropertiesUpdater
        implements JdbcDataSourcePropertiesUpdater {

    public SqlServerJdbcDataSourcePropertiesUpdater(AzureSqlProperties azureSqlProperties, AzureAdmin azureAdmin) {
        super(azureSqlProperties, DatabaseType.SQLSERVER, azureAdmin);
    }

    @Override
    String getUserName() {
        SqlServer sqlServer = azureAdmin.getSqlServer(azureSqlProperties.getServerName());
        if (sqlServer == null) {
            throw new IllegalArgumentException("SqlServer not found. If you want to auto create sqlServer. Please" +
                    " provide username and password");
        }

        return sqlServer.administratorLogin();
    }

    @Override
    String getUrl(DataSourceProperties dataSourceProperties) {
        SqlServer sqlServer = azureAdmin
                .getOrCreateSqlServer(azureSqlProperties.getServerName(), dataSourceProperties.getUsername(),
                        dataSourceProperties.getPassword());
        azureAdmin.getOrCreateSqlDatabase(azureSqlProperties.getServerName(), azureSqlProperties.getDatabaseName());
        return String.format(databaseType.getJdbcUrlTemplate(), sqlServer.fullyQualifiedDomainName(),
                azureSqlProperties.getDatabaseName());
    }
}
