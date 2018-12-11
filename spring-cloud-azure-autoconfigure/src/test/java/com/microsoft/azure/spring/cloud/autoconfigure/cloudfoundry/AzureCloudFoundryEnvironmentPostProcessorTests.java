/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.cloudfoundry;

import com.microsoft.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageProperties;
import org.junit.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Warren Zhu
 */
public class AzureCloudFoundryEnvironmentPostProcessorTests {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withInitializer(
            context -> new AzureCloudFoundryEnvironmentPostProcessor()
                    .postProcessEnvironment(context.getEnvironment(), null)).withUserConfiguration(
            AzureCfEnvPPTestConfiguration.class);

    @Test
    public void testConfigurationProperties() throws IOException {
        String vcapFileContents =
                new String(Files.readAllBytes(new ClassPathResource("VCAP_SERVICES").getFile().toPath()));
        this.contextRunner.withSystemProperties("VCAP_SERVICES=" + vcapFileContents).run(context -> {
            AzureServiceBusProperties serviceBusProperties = context.getBean(AzureServiceBusProperties.class);
            assertThat(serviceBusProperties.getConnectionString()).isEqualTo(
                    "Endpoint=sb://fake.servicebus.windows.net/;" +
                            "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=fakekey=");

            AzureStorageProperties storageProperties = context.getBean(AzureStorageProperties.class);
            assertThat(storageProperties.getAccount()).isEqualTo("fake");
            assertThat(storageProperties.getAccessKey()).isEqualTo("fakekey==");

        });
    }

    @EnableConfigurationProperties({AzureServiceBusProperties.class, AzureStorageProperties.class})
    static class AzureCfEnvPPTestConfiguration {

    }
}
