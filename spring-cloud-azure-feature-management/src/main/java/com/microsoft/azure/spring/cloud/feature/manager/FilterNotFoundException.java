/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

public class FilterNotFoundException extends Exception{
    
    private static final long serialVersionUID = 1L;
    
    private final FeatureFilterEvaluationContext filter;
    
    public FilterNotFoundException(String message, Throwable cause, FeatureFilterEvaluationContext filter) {
        super(message, cause);
        this.filter = filter;
    }
    
    @Override
    public String getMessage() {
        if (filter == null) {
            return getCause().getMessage();
        }
        return getCause().getMessage() + ", " + filter.toString();
        
    }

}
