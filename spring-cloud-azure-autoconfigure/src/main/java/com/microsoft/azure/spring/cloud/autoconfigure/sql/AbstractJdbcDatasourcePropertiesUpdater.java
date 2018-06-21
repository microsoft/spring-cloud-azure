/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Base class of {@link JdbcDataSourcePropertiesUpdater}
 *
 * @author Warren Zhu
 */
public abstract class AbstractJdbcDatasourcePropertiesUpdater implements JdbcDataSourcePropertiesUpdater {
    protected static final Log LOGGER = LogFactory.getLog(SqlServerJdbcDataSourcePropertiesUpdater.class);

    protected final DatabaseType databaseType;
    protected final AzureSqlProperties azureSqlProperties;
    protected final AzureAdmin azureAdmin;

    public AbstractJdbcDatasourcePropertiesUpdater(AzureSqlProperties azureSqlProperties, DatabaseType databaseType,
            AzureAdmin azureAdmin) {
        this.azureSqlProperties = azureSqlProperties;
        this.databaseType = databaseType;
        this.azureAdmin = azureAdmin;
        Assert.hasText(this.azureSqlProperties.getDatabaseName(), "A database name must be provided.");
        Assert.hasText(this.azureSqlProperties.getServerName(), "A database server must be provided.");
    }

    @Override
    public void updateDataSourceProperties(DataSourceProperties dataSourceProperties) {
        Assert.hasText(dataSourceProperties.getPassword(), "spring.datasource.username must not be empty");

        if (StringUtils.isEmpty(dataSourceProperties.getUsername())) {
            dataSourceProperties.setUsername(getUserName());
            LOGGER.info(String.format("spring.datasource.username is auto config into '%s'",
                    getUserName()));
        }

        if (StringUtils.isEmpty(dataSourceProperties.getDriverClassName())) {
            dataSourceProperties.setDriverClassName(getDriverClass());
        } else {
            LOGGER.warn("spring.datasource.driver-class-name is specified. " +
                    "Not using generated Cloud SQL configuration");
        }

        if (StringUtils.isEmpty(dataSourceProperties.getUrl())) {
            dataSourceProperties.setUrl(getUrl(dataSourceProperties));
        } else {
            LOGGER.warn("spring.datasource.url is specified. " + "Not using generated Cloud SQL configuration");
        }
    }

    String getDriverClass(){
        return databaseType.getJdbcDriverName();
    }

    abstract String getUserName();
    abstract String getUrl(DataSourceProperties dataSourceProperties);
}
