/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.mock;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Locale;

/**
 * Mock implementation of CloseableHttpResponse in order to store test Response data
 */
public class MockCloseableHttpResponse implements CloseableHttpResponse {
    private StatusLine statusLine;
    private HttpEntity entity;

    public MockCloseableHttpResponse(StatusLine statusLine, HttpEntity entity) {
        this.statusLine = statusLine;
        this.entity = entity;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        this.statusLine = statusline;
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {

    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {

    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {

    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {

    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public Header[] getHeaders(String name) {
        return new Header[0];
    }

    @Override
    public Header getFirstHeader(String name) {
        return null;
    }

    @Override
    public Header getLastHeader(String name) {
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        return new Header[0];
    }

    @Override
    public void addHeader(Header header) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setHeader(Header header) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void setHeaders(Header[] headers) {

    }

    @Override
    public void removeHeader(Header header) {

    }

    @Override
    public void removeHeaders(String name) {

    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(HttpParams params) {

    }
}
