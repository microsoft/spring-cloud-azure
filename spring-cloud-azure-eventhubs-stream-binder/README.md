# Spring Cloud Azure Event Hub Stream Binder

The project provides **Spring Cloud Stream Binder for Azure Event Hub** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Event Hub](https://azure.microsoft.com/en-us/services/event-hubs/) service.

## EventHub Binder Overview

The Spring Cloud Stream Binder for Azure Event Hub provides the binding implementation for the Spring Cloud Stream.
This implementation uses Spring Integration Event Hub Channel Adapters at its foundation. From design's perspective, 
Event Hub is similar as Kafka. Also, Event Hub could be accessed via Kafka API. If your project has tight dependency 
on Kafka API, you can try [Event Hub with Kafka API Sample](../spring-cloud-azure-samples/spring-cloud-azure-eventhub-kafka-sample/)

### Consumer Group

Event Hub provides similar support of consumer group as Apache Kafka, but with slight different logic. While Kafka 
stores all committed offsets in the broker, you have to store offsets of event hub messages 
being processed manually. Event Hub SDK provide the function to store such offsets inside Azure Storage Account. So 
that's why you have to fill `spring.cloud.stream.eventhub.checkpoint-storage-account`.

### Partitioning Support

Event Hub provides similar concept of physical partition as Kafka. But unlike Kafka's auto rebalancing between 
consumers and partitions, Event Hub provides a kind of preemptive mode. Storage account acts as lease to 
determine which partition is owned by which consumer. When a new consumer starts, it will try to steal some partitions 
from most heavy-loaded consumer to achieve workload balancing.

## Samples 

Please use this [sample](../spring-cloud-azure-samples/spring-cloud-azure-eventhub-binder-sample/) as a reference for how to use this binder. 

## Feature List 

- [Dependency Management](#dependency-management)
- [Configuration Options](#configuration-options)

### Dependency Management

**Maven Coordinates** 
```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-eventhub-stream-binder</artifactId>
</dependency>

```
**Gradle Coordinates** 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-eventhub-stream-binder'
}
```

### Configuration Options 

The binder provides the following configuration options in `application.properties`.

#### Spring Cloud Azure Properties ####

Name | Description | Required | Default 
---|---|---|---
 spring.cloud.azure.credential-file-path | Location of azure credential file | Yes |
 spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
 spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
 spring.cloud.azure.eventhub.namespace | Event Hub Namespace. Auto creating if missing | Yes |
 spring.cloud.azure.eventhub.checkpoint-storage-account | StorageAccount name for checkpoint message successfully consumed | Yes

 #### Event Hub Producer Properties ####

 It supports the following configurations with the format of `spring.cloud.stream.eventhub.bindings.<channelName>.producer`.
 
 **_sync_**
 
 Whether the producer should act in a synchronous manner with respect to writing records into a stream. If true, the 
 producer will wait for a response from Event Hub after a send operation.

 Default: `false`

 **_sendTimeout_**

 Effective only if `sync` is set to true. The amount of time to wait for a response from Event Hub after a send operation, in milliseconds.

 Default: `10000`
 
 #### Event Hub Consumer Properties ####

  It supports the following configurations with the format of `spring.cloud.stream.eventhub.bindings.<channelName>.consumer`.

  **_startPosition_**

  Whether the consumer receives messages from the beginning or end of event hub. if `EARLIEST`, from beginning. If 
  `LATEST`, from end.

  Default: `LATEST`

  **_checkpointMode_**

  The mode in which checkpoints are updated.
  
  If `RECORD`, checkpoints occur after each record is received by Spring Channel. If you use `StorageAccount` as checkpoint store, this might become botterneck.
  
  If `BATCH`, checkpoints occur after each batch of records is received by Spring Channel. This is the default mode if you can tolerate failure during message processing. That means once your processor receives (the actual processing of the message hasn't started yet) the message, the receipt of the message will be acknowledged.
  
  If `MANUAL`, checkpoints occur on demand by the user via the `Checkpointer`. You can do checkpoints after the message has been successfully processed. `Message.getHeaders.get(AzureHeaders.CHECKPOINTER)`callback can get you the `Checkpointer` you need. Please be aware all messages in the corresponding Event Hub partition before this message will be considered as successfully processed.

  Default: `BATCH`
