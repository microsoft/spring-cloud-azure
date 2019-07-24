/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.microsoft.azure.spring.cloud.feature.manager","com.example"})
public class FeatureManagerWebApplication {


	public static void main(String[] args) {
		SpringApplication.run(FeatureManagerWebApplication.class, args);
	}

}
