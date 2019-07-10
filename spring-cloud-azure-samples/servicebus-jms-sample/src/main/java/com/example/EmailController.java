package com.example;

import java.io.Serializable;

// Add Serializable
public class EmailController implements Serializable {

    // Serializer ID
    private static final long serialVersionUID = -295422703255886286L;

    private String to;      // destination
    private String body;    // content

    public EmailController() {
    }

    public EmailController(String to, String body) {
        this.to = to;
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("Email{to=%s, body=%s}", getTo(), getBody());
    }

}
