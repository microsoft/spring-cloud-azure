package com.microsoft.azure.spring.cloud.autoconfigure.jms;

public class ServiceBusKey {
    private final String host;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    ServiceBusKey(String host, String sharedAccessKeyName, String sharedAccessKey) {
        this.host = host;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKey;
    }

//    public void setHost(String host) {
//        this.host = host;
//    }

    public String getHost() {
        return host;
    }

//    public void setSharedAccessKeyName(String sharedAccessKeyName) {
//        this.sharedAccessKeyName = sharedAccessKeyName;
//    }

    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

//    public void setSharedAccessKey(String sharedAccessKey) {
//        this.sharedAccessKey = sharedAccessKey;
//    }

    public String getSharedAccessKey() {
        return sharedAccessKey;
    }
}
