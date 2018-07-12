/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
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
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static com.microsoft.azure.spring.cloud.autoconfigure.common.AutoConfigureUtils.getServiceName;

/**
 * Provides Azure SQL instance connectivity through Spring JDBC by providing
 * database server name and database name.
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})
@ConditionalOnProperty(name = "spring.cloud.azure.sql.enabled")
@EnableConfigurationProperties({AzureSqlProperties.class, DataSourceProperties.class})
@AutoConfigureBefore({DataSourceAutoConfiguration.class, JndiDataSourceAutoConfiguration.class,
        XADataSourceAutoConfiguration.class})
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
public class AzureSqlAutoConfiguration {

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, getServiceName(AzureSqlAutoConfiguration.class));
    }

    /**
     * The Sql Server Configuration for the {@link SqlServerJdbcDataSourcePropertiesUpdater}
     * based on the {@link DatabaseType#SQLSERVER}.
     */
    @ConditionalOnClass(com.microsoft.sqlserver.jdbc.SQLServerDriver.class)
    @ConditionalOnMissingBean(JdbcDataSourcePropertiesUpdater.class)
    static class SqlServerJdbcInfoProviderConfiguration {

        @Bean
        public JdbcDataSourcePropertiesUpdater defaultSqlServerJdbcInfoProvider(AzureSqlProperties azureSqlProperties,
                AzureAdmin azureAdmin) {

            return new SqlServerJdbcDataSourcePropertiesUpdater(azureSqlProperties, azureAdmin);
        }
    }

    /**
     * The Configuration to populated {@link DataSourceProperties} bean
     * based on the cloud-specific properties.
     */
    @Configuration
    @Import({SqlServerJdbcInfoProviderConfiguration.class})
    static class CloudSqlDataSourcePropertiesConfiguration {

        @Bean
        @ConditionalOnBean(JdbcDataSourcePropertiesUpdater.class)
        public DataSourceProperties cloudSqlDataSourceProperties(DataSourceProperties dataSourceProperties,
                JdbcDataSourcePropertiesUpdater dataSourcePropertiesProvider) {

            dataSourcePropertiesProvider.updateDataSourceProperties(dataSourceProperties);

            return dataSourceProperties;
        }
    }
}

