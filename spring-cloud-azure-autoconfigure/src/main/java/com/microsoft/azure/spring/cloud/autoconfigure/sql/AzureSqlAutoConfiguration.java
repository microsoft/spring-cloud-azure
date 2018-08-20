/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Provides Azure SQL instance connectivity through Spring JDBC by providing
 * database server name and database name.
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})
@ConditionalOnProperty(name = "spring.cloud.azure.sql.enabled", matchIfMissing = true)
@EnableConfigurationProperties({AzureSqlProperties.class, DataSourceProperties.class})
@AutoConfigureBefore({DataSourceAutoConfiguration.class, JndiDataSourceAutoConfiguration.class,
        XADataSourceAutoConfiguration.class, TelemetryAutoConfiguration.class})
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
public class AzureSqlAutoConfiguration {
    private static final String SQL_SERVER = "SqlServer";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SQL_SERVER);
    }

    /**
     * The Sql Server Configuration for the {@link SqlServerJdbcDataSourcePropertiesUpdater}
     * based on the {@link DatabaseType#SQLSERVER}.
     */
    @ConditionalOnClass(com.microsoft.sqlserver.jdbc.SQLServerDriver.class)
    @ConditionalOnMissingBean(JdbcDataSourcePropertiesUpdater.class)
    class SqlServerJdbcInfoProviderConfiguration {

        @Bean
        public JdbcDataSourcePropertiesUpdater defaultSqlServerJdbcInfoProvider(AzureAdmin azureAdmin,
                AzureSqlProperties sqlProperties) {

            return new SqlServerJdbcDataSourcePropertiesUpdater(sqlProperties, azureAdmin);
        }
    }

    /**
     * The Configuration to populated {@link DataSourceProperties} bean
     * based on the cloud-specific properties.
     */
    @Configuration
    @Import({SqlServerJdbcInfoProviderConfiguration.class})
    class CloudSqlDataSourcePropertiesConfiguration {

        @Bean
        @Primary
        @ConditionalOnBean(JdbcDataSourcePropertiesUpdater.class)
        public DataSourceProperties cloudSqlDataSourceProperties(DataSourceProperties dataSourceProperties,
                JdbcDataSourcePropertiesUpdater dataSourcePropertiesProvider) {

            dataSourcePropertiesProvider.updateDataSourceProperties(dataSourceProperties);

            return dataSourceProperties;
        }
    }
}

