/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.method.HandlerMethod;

/**
 * Unit test for simple App.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeatureHandlerTest {

    @InjectMocks
    FeatureHandler featureHandler;

    @Mock
    FeatureManager featureManager;

    @Mock
    FeatureManagerSnapshot featureManagerSnapshot;

    @Mock
    IDisabledFeaturesHandler disabledFeaturesHandler;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    HandlerMethod handlerMethod;

    @Test
    public void preHandleNotHandler() {
        assertTrue(featureHandler.preHandle(request, response, new Object()));
    }

    @Test
    public void preHandleNoFeatureOn() throws NoSuchMethodException, SecurityException {
        Method method = TestClass.class.getMethod("noAnnotation");
        when(handlerMethod.getMethod()).thenReturn(method);

        assertTrue(featureHandler.preHandle(request, response, handlerMethod));
    }
    
    @Test
    public void preHandlFeatureOn() throws NoSuchMethodException, SecurityException {
        Method method = TestClass.class.getMethod("featureOnAnnotation");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(featureManager.isEnabled(Mockito.matches("test"))).thenReturn(true);

        assertTrue(featureHandler.preHandle(request, response, handlerMethod));
    }
    
    @Test
    public void preHandlFeatureOnSnapshot() throws NoSuchMethodException, SecurityException {
        Method method = TestClass.class.getMethod("featureOnAnnotationSnapshot");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(featureManagerSnapshot.isEnabled(Mockito.matches("test"))).thenReturn(true);

        assertTrue(featureHandler.preHandle(request, response, handlerMethod));
    }
    
    @Test
    public void preHandlFeatureOnNotEnabled() throws NoSuchMethodException, SecurityException {
        Method method = TestClass.class.getMethod("featureOnAnnotation");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(featureManager.isEnabled(Mockito.matches("test"))).thenReturn(false);

        assertFalse(featureHandler.preHandle(request, response, handlerMethod));
    }
    
    @Test
    public void preHandlFeatureOnRedirect() throws NoSuchMethodException, SecurityException {
        Method method = TestClass.class.getMethod("featureOnAnnotaitonRedirected");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(featureManager.isEnabled(Mockito.matches("test"))).thenReturn(false);
        

        assertFalse(featureHandler.preHandle(request, response, handlerMethod));
    }

    protected class TestClass {

        public void noAnnotation() {}
        
        @FeatureGate(feature = "test")
        public void featureOnAnnotation() {}
        
        @FeatureGate(feature = "test", snapshot = true)
        public void featureOnAnnotationSnapshot() {}
        
        @FeatureGate(feature = "test", fallback = "/redirected")
        public void featureOnAnnotaitonRedirected() {}

    }

}
