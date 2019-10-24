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

    public static Boolean watchRequests = false;

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String sdkUserAgent = context.getHttpRequest().getHeaders().get(HttpHeaders.USER_AGENT).getValue();
        context.getHttpRequest().getHeaders().put(HttpHeaders.USER_AGENT, USER_AGENT + " " + sdkUserAgent);
        context.getHttpRequest().getHeaders().put("Correlation-Context", getTracingInfo(context.getHttpRequest()));
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
        String requestTypeValue = RequestType.WATCH.toString();
        if (!watchRequests) {
            requestTypeValue = request.getUrl().getPath().startsWith("/kv") ? RequestType.STARTUP.toString()
                    : RequestType.WATCH.toString();
        }
        if (requestTypeValue.equals(RequestType.WATCH.toString())) {
            watchRequests = true;
        }
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
