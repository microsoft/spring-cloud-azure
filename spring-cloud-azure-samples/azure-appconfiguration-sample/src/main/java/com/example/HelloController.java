/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@RestController
public class HelloController {
    private final MessageProperties properties;
    private final FeatureManager featureManager;
    private final Second second;
    private final KeyVault keyVault;

    public HelloController(MessageProperties properties, FeatureManager featureManager, Second second, KeyVault keyVault) {
        this.properties = properties;
        this.featureManager = featureManager;
        this.second = second;
        this.keyVault = keyVault;
    }

    @GetMapping
    public String getMessage() {
        String text = "<h1>Spring Cloud Azure App Configuration Demo</h1>";
        text += "<ul><li><b>Key Value from First Resource</b> - " + properties.getMessage();
        text += "</li><li><b>FeatureA</b> - ";
        if (featureManager.isEnabled("FeatureA")) {
            text += "On";
        } else {
            text += "Off";
        }
        text += "</li><li><b>Feature from Second Resource</b> - ";
        if (featureManager.isEnabled("Second")) {
            text += "On";
        } else {
            text += "Off";
        }
        text += "</li><li><b>Key Value from Second Source</b> - " + second.getSource();
        text += "</li><li><b>Key Vault Value</b> - " + keyVault.getSecret();
        return text;
    }
}
