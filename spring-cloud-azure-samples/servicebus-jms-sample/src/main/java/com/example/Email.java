/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import java.io.Serializable;

public class Email implements Serializable {

    private static final long serialVersionUID = -295422703255886286L;

    private String destination;
    private String content;

    public Email(String destination, String content) {
        setDestination(destination);
        setContent(content);
    }

    public String getDestination() { return destination; }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format("Email{destination=%s, content=%s}", getDestination(), getContent());
    }

}
