package com.example;

import java.io.Serializable;

public class EmailController implements Serializable {

    private static final long serialVersionUID = -295422703255886286L;

    private String destination;
    private String content;

    public EmailController() {
    }

    public EmailController(String destination, String content) {
        this.destination = destination;
        this.content = content;
    }

    public String getDestination() {
        return destination;
    }

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
