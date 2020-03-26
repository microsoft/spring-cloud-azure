/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.cloud.config.AppConfigurationProviderProperties;

@ControllerEndpoint(id = "appconfig-refresh")
public class AppConfigurationRefreshEndpoint {

    private ContextRefresher contextRefresher;

    private ObjectMapper objectmapper = new ObjectMapper();

    private AppConfigurationProviderProperties appConfiguration;

    public AppConfigurationRefreshEndpoint(ContextRefresher contextRefresher,
            AppConfigurationProviderProperties appConfiguration) {
        this.contextRefresher = contextRefresher;
        this.appConfiguration = appConfiguration;
    }

    @PostMapping(value = "/")
    @ResponseBody
    public String refresh(HttpServletRequest request, HttpServletResponse response,
            @RequestParam Map<String, String> allRequestParams) throws IOException {
        if (appConfiguration.getTokenName() == null || appConfiguration.getTokenSecret() == null
                || !allRequestParams.containsKey(appConfiguration.getTokenName())
                || !allRequestParams.get(appConfiguration.getTokenName()).equals(appConfiguration.getTokenSecret())) {
            return HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }

        String reference = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        JsonNode kvReference = objectmapper.readTree(reference);

        JsonNode validationResponse = kvReference.findValue("validationCode");
        if (validationResponse != null) {
            // Validating Web Hook
            return "{ \"validationResponse\": \"" + validationResponse.asText() + "\"}";
        } else {
            if (contextRefresher != null) {
                // Will just refresh the local configurations
                System.out.println("Standard Refresh");
                contextRefresher.refresh();
            }
        }

        return HttpStatus.OK.getReasonPhrase();
    }
}
