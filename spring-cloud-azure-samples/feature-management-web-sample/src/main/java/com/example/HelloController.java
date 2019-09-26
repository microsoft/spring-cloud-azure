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

import com.microsoft.azure.spring.cloud.feature.manager.FeatureGate;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManagerSnapshot;

@Controller
@ConfigurationProperties("controller")
public class HelloController {

    @Autowired
    private FeatureManager featureManager;
    
    @Autowired
    private FeatureManagerSnapshot featureManagerSnapshot;
    
    @GetMapping("/privacy")
    public String getRequestBased(Model model) {
        model.addAttribute("isDarkThemeS1", featureManagerSnapshot.isEnabled("DarkTheme"));
        model.addAttribute("isDarkThemeS2", featureManagerSnapshot.isEnabled("DarkTheme"));
        model.addAttribute("isDarkThemeS3", featureManagerSnapshot.isEnabled("DarkTheme"));
        return "privacy";
    }

    @GetMapping(value= {"/Beta","/BetaA"})
    @FeatureGate(feature = "BetaAB", fallback = "/BetaB")
    public String getRedirect(Model model) {
        return "BetaA";
    }

    @GetMapping("/BetaB")
    public String getRedirected(Model model) {
        return "BetaB";
    }

    @GetMapping(value= {"", "/", "/welcome"})
    public String mainWithParam(Model model) {
        model.addAttribute("Beta", featureManager.isEnabled("Beta"));
        return "welcome";
    }
}
