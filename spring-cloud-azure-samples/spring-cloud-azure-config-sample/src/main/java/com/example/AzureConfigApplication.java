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
    @Value("${abc.def}")
    private String foovalue;

    @Value("${my.test.value}")
    private String myTestyValue;

    public static void main(String[] args) {
        SpringApplication.run(AzureConfigApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("foovalue: " + foovalue);
        System.out.println("myTestyValue: " + myTestyValue);

        System.exit(0);
    }
}
