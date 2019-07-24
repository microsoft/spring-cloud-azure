/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeatureManagerSnapshotTest {

    @InjectMocks
    FeatureManagerSnapshot featureManagerSnapshot;

    @Mock
    FeatureManager featureManager;

    @Mock
    HttpServletRequest request;

    @Test
    public void setAttribute() {
        when(featureManager.isEnabled(Mockito.matches("setAttribute"))).thenReturn(true);
        when(request.getAttribute(Mockito.matches("setAttribute"))).thenReturn(null).thenReturn(true).thenReturn(true);

        assertTrue(featureManagerSnapshot.isEnabled("setAttribute"));
        assertTrue(featureManagerSnapshot.isEnabled("setAttribute"));
        verify(featureManager, times(1)).isEnabled("setAttribute");
    }

}
