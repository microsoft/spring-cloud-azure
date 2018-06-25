/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Update {@link DataSourceProperties} based on {@link AzureSqlProperties}
 *
 * @author Warren Zhu
 */
public interface JdbcDataSourcePropertiesUpdater {

    void updateDataSourceProperties(DataSourceProperties dataSourceProperties);
}
