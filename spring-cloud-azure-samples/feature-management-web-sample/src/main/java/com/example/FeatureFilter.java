/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@Component
@Order(1)
public class FeatureFilter implements Filter {
    
    Logger logger = LoggerFactory.getLogger(FeatureFilter.class);
    
    @Autowired
    FeatureManager featureManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if(!featureManager.isEnabled("Beta")) {
            logger.info("skip new Beta filter");
            chain.doFilter(request, response);
            return;
        }
        logger.info("Run the Beta filter");
        chain.doFilter(request, response); 
    }
}
