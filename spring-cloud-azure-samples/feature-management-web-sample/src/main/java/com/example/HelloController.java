/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureGate;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManagerSnapshot;

@Controller
@ConfigurationProperties("controller")
public class HelloController {
    
    @Autowired
    private MessageProperties properties;

    @Autowired
    private FeatureManager featureManager;
    
    @Autowired
    private FeatureManagerSnapshot featureManagerSnapshot;

    @Autowired
    private TestComponent testComponent;

    @GetMapping("/")
    @FeatureGate(feature = "FeatureV")
    @ResponseBody
    public String getMessage() {
        return "Message: " + properties.getMessage();
    }
    
    @GetMapping("/requestBased")
    @ResponseBody
    public String getRequestBased() {
        String result = "";
        for (int i = 0; i < 100; i++) {
            result += " " + featureManagerSnapshot.isEnabled("FeatureV");
        }
        return result;
    }

    @GetMapping("/test")
    @ResponseBody
    public String getTest() {
        return testComponent.test();
    }

    @GetMapping("/redirect")
    @FeatureGate(feature = "FeatureV", fallback = "/redirected")
    @ResponseBody
    public String getRedirect() {
        return "Redirect";
    }

    @GetMapping("/redirected")
    @ResponseBody
    public String getRedirected() {
        return "Redirected";
    }

    @GetMapping("/welcome")
    public String mainWithParam(
            @RequestParam(name = "name", required = false, defaultValue = "") String name, Model model) {
        if (featureManager.isEnabled("FeatureV")) {
            model.addAttribute("message", "Beta User");
        } else {
            model.addAttribute("message", name);
        }
        return "welcome";
    }
}
