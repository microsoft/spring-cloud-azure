/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import java.time.LocalDateTime;

/**
 * @author Warren Zhu
 */
public class UserMessage {

    private String body;

    private String username;

    private LocalDateTime createdAt;

    public UserMessage() {
    }

    public UserMessage(String body, String username, LocalDateTime createdAt) {
        this.body = body;
        this.username = username;
        this.createdAt = createdAt;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserMessage{" + "body='" + body + '\'' + ", username='" + username + '\'' + ", createdAt=" + createdAt +
                '}';
    }
}
