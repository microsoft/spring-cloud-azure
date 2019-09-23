/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

/**
 * Request Tracing values used to check 
 */
enum RequestTracingConstants {
    
    AZURE_APP_CONFIGURATION_TRACING_DISABLED("AZURE_APP_CONFIGURATION_TRACING_DISABLED"),
    FUNCTIONS_EXTENSION_VERSION("FUNCTIONS_EXTENSION_VERSION"),
    WEBSITE_NODE_DEFAULT_VERSION("WEBSITE_NODE_DEFAULT_VERSION"),
    REQUEST_TYPE("RequestType"),
    HOST("Host");
    
    private final String text;
    
    /**
     * @param text
     */
    RequestTracingConstants(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

}
