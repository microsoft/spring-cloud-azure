# Spring Cloud Azure Event Hub Stream Binder

The project provides **Spring Cloud Stream Binder for Azure Event Hub** which allows you to build your message-driven applications using Spring programming model and [Azure Event Hub](https://azure.microsoft.com/en-us/services/event-hubs/) service. 

## Samples 

Please use this [sample](../spring-cloud-azure-eventhub-binder-sample/) as a reference for how to use Spring Cloud Stream Binder for Azure Event Hub in your projects. 

## Feature List 

- [Dependency Management](#dependency-management)
- [Configuration Options](#configuration-options)

### Dependency Management

Please use [`spring-cloud-azure-starter-eventhub`](spring-cloud-azure-starters/spring-cloud-azure-starter-eventhub/) to auto-configure Azure Event Hub and `spring-cloud-azure-eventhub-stream-binder` in your project. 

**Maven Coordinates** 
```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-starter-eventhub</artifactId>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-eventhub-stream-binder</artifactId>
</dependency>

```
**Gradle Coordinates** 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-starter-eventhub'
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-eventhub-stream-binder'
}
```

### Configuration Options 

The [`spring-cloud-azure-starter-eventhub`](spring-cloud-azure-starters/spring-cloud-azure-starter-eventhub/) provides the following configuration options in `application.properties`.

#### Spring Cloud Azure Properties ####

Name | Description | Required | Default 
---|---|---|---
 spring.cloud.azure.credentialFilePath | Location of azure credential file | Yes | 
 spring.cloud.azure.resourceGroup | Name of Azure resource group | Yes | 
 spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
 spring.cloud.azure.eventhub.namespace | Namespace of the Event Hub. Will create a new one if not existing | Yes |
 spring.cloud.stream.eventhub.checkpointStorageAccount |  | Yes | 
 
 #### Event Hub Binder Properties ####
 It supports the following configurations with the predix of "spring.cloud.stream.eventhub.binder."
 
 **_headers_**
 
The set of custom headers to transfer over Event Hub

Default: "correlationId", "sequenceSize", "sequenceNumber", "contentType", "originalContentType".

 #### Event Hub Producer Properties ####

 It supports the following configurations with the format of "spring.cloud.stream.eventhub.<channelName>.producer". 
 
 **_sync_**
 
 Effective only if sync is set to true. The amount of time to wait for a response from Kinesis after a PutRecord operation, in milliseconds.

 Default: 10000
 
