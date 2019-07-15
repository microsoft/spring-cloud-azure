package com.microsoft.azure.spring.cloud.autoconfigure.jms;

public class ServiceBusKey {
    private String host;
    private String sharedAccessKeyName;
    private String sharedAccessKey;

    ServiceBusKey() {
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setSharedAccessKeyName(String sharedAccessKeyName) {
        this.sharedAccessKeyName = sharedAccessKeyName;
    }

    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    public void setSharedAccessKey(String sharedAccessKey) {
        this.sharedAccessKey = sharedAccessKey;
    }

    public String getSharedAccessKey() {
        return sharedAccessKey;
    }
}
