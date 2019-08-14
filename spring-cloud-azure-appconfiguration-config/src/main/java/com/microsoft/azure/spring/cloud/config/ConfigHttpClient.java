/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_256;

/**
 * Util class to execute http request, before sending http request, valid request headers
 * will be added for each request based on given credential ID and secret.
 *
 * How to use:
 * <p>
 * HttpGet httpGet = new HttpGet("https://my-config-store.azconfig.io/keys");
 * CloseableHttpResponse response = ConfigHttpClient.execute(httpGet, "my-credential",
 * "my-secret");
 * <p/>
 */
public class ConfigHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHttpClient.class);
    private static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private static final String PACKAGE_NAME = ConfigHttpClient.class.getPackage().getImplementationTitle();
    public static final String USER_AGENT = String.format("%s/%s", StringUtils.remove(PACKAGE_NAME, " "), 
            ConfigHttpClient.class.getPackage().getImplementationVersion());

    static {
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final CloseableHttpClient httpClient;

    public ConfigHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpResponse execute(@NonNull HttpUriRequest request, Date date, String credential, String secret)
            throws IOException, URISyntaxException {
        Assert.notNull(request, "Request should not be null.");

        Map<String, String> authHeaders = buildRequestHeaders(request, date, credential, secret);
        authHeaders.forEach(request::setHeader);

        return httpClient.execute(request);
    }

    /**
     * Generates request Headers; date, client request id, and a sha256 content
     * hash.
     * 
     * @param request the request that will be sent with this header and will have a hash
     * generated for it.
     * @param date the current date and time
     * @param credential Access key ID
     * @param secret Access key value
     * @return map of the header values and keys
     * @throws URISyntaxException will be thrown when the request URI isn't valid
     * @throws IOException will be thrown when request content fails to convert to UTF-8
     */
    private static Map<String, String> buildRequestHeaders(HttpUriRequest request, Date date, String credential,
            String secret) throws URISyntaxException, IOException {
        String requestTime = GMT_DATE_FORMAT.format(date);
        String contentHash = buildContentHash(request);

        // SignedHeaders
        String signedHeaders = "x-ms-date;host;x-ms-content-sha256";

        // Signature
        String signature = buildSignature(request, requestTime, contentHash, secret);

        // Compose headers
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ms-date", requestTime);
        headers.put("x-ms-client-request-id", UUID.randomUUID().toString());
        headers.put("x-ms-content-sha256", contentHash);
        headers.put("correlation-context", getTracingInfo(request));

        String authorization = String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s",
                credential, signedHeaders, signature);
        headers.put("Authorization", authorization);

        headers.put(HttpHeaders.USER_AGENT, USER_AGENT);

        return headers;
    }

    private static String buildContentHash(HttpUriRequest request) throws IOException {
        String content = "";
        if (request instanceof HttpEntityEnclosingRequest) {
            content = copyInputStream(((HttpEntityEnclosingRequest) request).getEntity().getContent());
        }

        byte[] digest = new DigestUtils(SHA_256).digest(content);
        return Base64.getEncoder().encodeToString(digest);
    }

    private static String buildSignature(HttpUriRequest request, String requestTime, String contentHash, String secret)
            throws URISyntaxException {
        // String-To-Sign
        String methodName = request.getRequestLine().getMethod().toUpperCase();
        String requestPath = getRequestPath(request);
        String host = getHost(request);
        String toSign = String.format("%s\n%s\n%s;%s;%s", methodName, requestPath, requestTime, host, contentHash);

        // Signature
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return encodeHmac(HMAC_SHA_256, decodedKey, toSign);
    }

    // Extract request path and query params, e.g., https://example.com/abc?param=xyz ->
    // /abc?param=xyz
    private static String getRequestPath(HttpRequest request) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(request.getRequestLine().getUri());
        
        String scheme = uri.getScheme() + "://";

        return uri.toString().substring(scheme.length()).substring(uri.getHost().length());
    }

    private static String getHost(HttpRequest request) throws URISyntaxException {
        return new URIBuilder(request.getRequestLine().getUri()).getHost();
    }

    private static String encodeHmac(HmacAlgorithms algorithm, byte[] key, String data) {
        return Base64.getEncoder().encodeToString(new HmacUtils(algorithm, key).hmac(data));
    }

    private static String copyInputStream(InputStream inputStream) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);

            return writer.toString();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.trace("Failed to close the input stream.", e);
            }
        }
    }
    
    /**
     * Checks if Azure App Configuration Tracing is disabled, and if not gets tracing
     * information.
     * 
     * @param request The http request that will be traced, used to check operation being run.
     * @return String of the value for the correlation-context header. 
     */
    private static String getTracingInfo(HttpUriRequest request) {
        String track = System.getenv(RequestTracingConstants.AZURE_APP_CONFIGURATION_TRACING_DISABLED.toString());
        if (track != null && track.equalsIgnoreCase("false")) {
            return "";
        }
        String requestTypeValue = request.getURI().getPath().startsWith("/kv") ? RequestType.STARTUP.toString()
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
