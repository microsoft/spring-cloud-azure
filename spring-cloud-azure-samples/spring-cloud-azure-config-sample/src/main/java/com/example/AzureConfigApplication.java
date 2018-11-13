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

@SpringBootApplication
public class AzureConfigApplication implements CommandLineRunner {
    @Value("${azure.config.test}")
    private String remoteValue;

    @Value("${azure.local.test.value}")
    private String localValue;

    public static void main(String[] args) {
        SpringApplication.run(AzureConfigApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("remoteValue: " + remoteValue);
        System.out.println("localValue: " + localValue);

        System.exit(0);
    }
}
