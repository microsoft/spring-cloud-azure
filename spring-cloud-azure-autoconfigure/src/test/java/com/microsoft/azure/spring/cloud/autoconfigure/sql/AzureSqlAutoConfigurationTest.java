/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.sql;

import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureSqlAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(AzureContextAutoConfiguration.class, AzureSqlAutoConfiguration.class))
                                                                                   .withUserConfiguration(
                                                                                           TestConfiguration.class);

    @Test
    public void testWithoutAzureSqlProperties() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureSqlProperties.class));
    }

    @Test
    public void testAzureSqlPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.sql.databaseName=db1").
                withPropertyValues("spring.cloud.azure.sql.enabled=true").
                                  withPropertyValues("spring.cloud.azure.sql.serverName=server1").
                                  withPropertyValues("spring.datasource.password=ps1").run(context -> {
            assertThat(context).hasSingleBean(AzureSqlProperties.class);
            assertThat(context.getBean(AzureSqlProperties.class).getDatabaseName()).isEqualTo("db1");
            assertThat(context.getBean(AzureSqlProperties.class).getServerName()).isEqualTo("server1");
        });
    }

    @Test
    public void testNoJdbc() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.sql.enabled=true")
                          .withClassLoader(new FilteredClassLoader(EmbeddedDatabaseType.class, DataSource.class))
                          .run(context -> {
                              assertThat(context).doesNotHaveBean(DataSource.class);
                              assertThat(context).doesNotHaveBean(DataSourceProperties.class);
                          });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        AzureAdmin azureAdmin() {
            AzureAdmin azureAdmin = mock(AzureAdmin.class);
            SqlDatabase sqlDatabase = mock(SqlDatabase.class);
            SqlServer sqlServer = mock(SqlServer.class);
            when(sqlServer.administratorLogin()).thenReturn("user");
            when(azureAdmin.getSqlServer("server1")).thenReturn(sqlServer);
            when(azureAdmin.getOrCreateSqlServer(isA(String.class), isA(String.class), isA(String.class)))
                    .thenReturn(sqlServer);
            when(azureAdmin.getOrCreateSqlDatabase("server1", "db1")).thenReturn(sqlDatabase);
            when(sqlServer.fullyQualifiedDomainName()).thenReturn("");

            return azureAdmin;
        }

    }
}
