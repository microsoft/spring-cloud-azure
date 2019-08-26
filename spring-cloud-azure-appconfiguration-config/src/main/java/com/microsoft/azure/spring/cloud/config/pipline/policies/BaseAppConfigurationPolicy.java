/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.pipline.policies;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.microsoft.azure.spring.cloud.config.HostType;
import com.microsoft.azure.spring.cloud.config.RequestTracingConstants;
import com.microsoft.azure.spring.cloud.config.RequestType;

import reactor.core.publisher.Mono;

public class BaseAppConfigurationPolicy implements HttpPipelinePolicy {

    private static final String PACKAGE_NAME = BaseAppConfigurationPolicy.class.getPackage().getImplementationTitle();

    public static final String USER_AGENT = String.format("%s/%s", StringUtils.remove(PACKAGE_NAME, " "),
            BaseAppConfigurationPolicy.class.getPackage().getImplementationVersion());
    
    int count = 0;

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        count++;
        System.out.println(count);
        String sdkUserAgent = context.httpRequest().headers().get(HttpHeaders.USER_AGENT).value();
        context.httpRequest().headers().put(HttpHeaders.USER_AGENT, USER_AGENT + "; " + sdkUserAgent);
        context.httpRequest().headers().put("Correlation-Context", getTracingInfo(context.httpRequest()));
        return next.process();
    }

    /**
     * Checks if Azure App Configuration Tracing is disabled, and if not gets tracing
     * information.
     * 
     * @param request The http request that will be traced, used to check operation being
     * run.
     * @return String of the value for the correlation-context header.
     * @throws URISyntaxException
     */
    private static String getTracingInfo(HttpRequest request) {
        String track = System.getenv(RequestTracingConstants.AZURE_APP_CONFIGURATION_TRACING_DISABLED.toString());
        if (track != null && track.equalsIgnoreCase("false")) {
            return "";
        }

        String requestTypeValue = request.url().getPath().startsWith("/kv") ? RequestType.STARTUP.toString()
                : RequestType.WATCH.toString();
        String requestType = RequestTracingConstants.REQUEST_TYPE.toString() + "=" + requestTypeValue;
        String host = RequestTracingConstants.HOST + "=" + getHostType();

        return requestType + "," + host;

    }

    /**
     * Gets the current host machines type; Azure Function, Azure Web App, or None.
     * 
     * @return String of Host Type
     */
    private static String getHostType() {
        String azureFunctionVersion = System.getenv(RequestTracingConstants.FUNCTIONS_EXTENSION_VERSION.toString());
        String azureWebsiteVersion = System.getenv(RequestTracingConstants.WEBSITE_NODE_DEFAULT_VERSION.toString());
        HostType hostType = azureFunctionVersion != null ? HostType.AZURE_FUNCTION
                : azureWebsiteVersion != null
                        ? HostType.AZURE_WEB_APP
                        : HostType.NONE;

        return hostType.toString();

    }

}
