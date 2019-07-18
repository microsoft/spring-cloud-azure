/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Intercepter for Requests to check if they should be run.
 */
@Component
public class FeatureHandler extends HandlerInterceptorAdapter {

    Logger logger = LoggerFactory.getLogger(FeatureHandler.class);

    @Autowired
    FeatureManager featureManager;

    @Autowired
    FeatureManagerSnapshot featureManagerSnapshot;

    @Autowired
    private IDisabledFeaturesHandler disabledFeaturesHandler;

    /**
     * Checks if the endpoint being called has the @FeatureOn annotation. Checks if the
     * feature is on. Can redirect if feature is off, or can return the disabled feature
     * handler.
     * 
     * @return true if the @FeatureOn annotation is on or the feature is enabled. Else, it
     * returns false, or is redirected.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Method method = null;
        if (handler instanceof HandlerMethod) {
            method = ((HandlerMethod) handler).getMethod();
        }
        if (method != null) {
            FeatureOn featureOn = method.getAnnotation(FeatureOn.class);
            if (featureOn != null) {
                String feature = featureOn.feature();
                Boolean snapshot = featureOn.snapshot();
                Boolean enabled = false;
                if (!snapshot) {
                    enabled = featureManager.isEnabled(feature);
                } else {
                    enabled = featureManagerSnapshot.isEnabled(feature);
                }
                if (!enabled && !featureOn.redirect().isEmpty()) {
                    try {
                        response.sendRedirect(featureOn.redirect());
                    } catch (IOException e) {
                        logger.info("Unable to send redirect.");
                    }
                }
                if (!enabled && disabledFeaturesHandler != null) {
                    response = disabledFeaturesHandler.handleDisabledFeatures(request, response);
                }
                return enabled;
            }
        }
        return true;
    }
}
