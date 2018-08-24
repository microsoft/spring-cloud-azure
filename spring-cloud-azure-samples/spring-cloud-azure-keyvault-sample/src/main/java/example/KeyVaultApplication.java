/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
public class KeyVaultApplication implements CommandLineRunner{
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultApplication.class);

    @Value("${keyVault:[secret-name]}")
    private String datasource;

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(KeyVaultApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Autowired Property 'datasource' has value: {}", datasource);
        LOGGER.info("Property 'datasource' from environment has value: {}", environment.getProperty
                ("keyVault:datasource"));
    }
}
