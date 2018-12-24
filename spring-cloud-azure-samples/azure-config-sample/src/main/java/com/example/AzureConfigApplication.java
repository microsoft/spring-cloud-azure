/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TestProperties.class)
public class AzureConfigApplication implements CommandLineRunner {
    @Value("${config.background-color}")
    private String remoteValue;

    public static void main(String[] args) {
        SpringApplication.run(AzureConfigApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("remoteValue: " + remoteValue);
    }
}
