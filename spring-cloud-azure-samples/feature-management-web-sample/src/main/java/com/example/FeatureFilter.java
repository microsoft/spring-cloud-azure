/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@Component
public class FeatureFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureFilter.class);
    
    @Autowired
    private FeatureManager featureManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if(!featureManager.isEnabledAsync("Beta").get()) {
                chain.doFilter(request, response);
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Request failed.", e);
        }
        LOGGER.info("Run the Beta filter");
        chain.doFilter(request, response); 
    }
}
