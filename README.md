[![Build Status](https://travis-ci.com/Microsoft/spring-cloud-azure.svg?branch=master)](https://travis-ci.com/Microsoft/spring-cloud-azure)
[![codecov.io](https://codecov.io/gh/Microsoft/spring-cloud-azure/coverage.svg?branch=master)](https://codecov.io/gh/Microsoft/spring-cloud-azure?branch=master)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/Microsoft/spring-cloud-azure/blob/master/LICENCE)

# Spring Cloud Azure

[Spring Cloud](http://projects.spring.io/spring-cloud/) provides boilerplate patterns for developers to quickly build and orchestrate their microservice based applications. Based on that, **Spring Cloud Azure** is designed to provide seamless Spring integration with Azure services. Developers can adopt a Spring-idiomatic way to take advantage of services on Azure, with only few lines of configuration and minimal code changes. 

## Module and Starter

### [Spring Cloud Stream](https://cloud.spring.io/spring-cloud-stream/)
Module | Version | Sample
------ |--- | ---
[Spring Cloud Stream Binder for Azure Event Hub](spring-cloud-azure-eventhub-stream-binder/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-eventhub-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-eventhub-stream-binder%22) |[Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/eventhub-binder-sample)
[Spring Cloud Stream with Azure Event Hub Kafka API](spring-cloud-azure-samples/eventhub-kafka-sample/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhub-kafka.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhub-kafka%22)|[Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/eventhub-kafka-sample)
[Spring Cloud Stream Binder for Azure Service Bus Topic](spring-cloud-azure-servicebus-topic-stream-binder/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-topic-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-topic-stream-binder%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/servicebus-topic-binder-sample)
[Spring Cloud Stream Binder for Azure Service Bus Queue](spring-cloud-azure-servicebus-queue-stream-binder/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-queue-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-queue-stream-binder%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/servicebus-queue-binder-sample)

### [Spring Integration](https://spring.io/projects/spring-integration)
Module | Version | Sample
------|--- | ---
[Spring Integration for Azure Event Hub](spring-integration-azure/spring-integration-eventhub) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-eventhub.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-eventhub%22) | [Integration Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/eventhub-integration-sample) [EventHubTemplate Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/eventhub-operation-sample)
[Spring Integration for Azure Service Bus](spring-integration-azure/spring-integration-servicebus) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-servicebus.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-servicebus%22)| [Integration Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/servicebus-integration-sample) [ServiceBusTemplate Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/servicebus-operation-sample)
[Spring Integration for Storage Queue](spring-integration-azure/spring-integration-storage-queue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-starter-azure-storage-queue.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-starter-azure-storage-queue%22)| [Integration Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/storage-queue-integration-sample) [StorageQueueTemplate Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/storage-queue-operation-sample)

### [Spring Resource](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources)

Module | Version | Sample
-------|--- | ---
[Spring Resource Abstraction for Azure Storage](spring-cloud-azure-storage/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-storage.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-storage%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/storage-sample)

### [Spring Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html)

Module | Version | Sample
-------|--- | ---
[Spring Caching with Azure Redis Cache](spring-cloud-azure-samples/spring-cloud-azure-cache-sample) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-starter-azure-cache.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-starter-azure-cache%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.RC1/spring-cloud-azure-samples/cache-sample)

## Sample and Tutorial 

Please use the [samples](spring-cloud-azure-samples/) as a reference for how to use Spring Cloud Azure in your projects. For more information about building Spring applications on Azure, please check [Spring on Azure tutorials](https://docs.microsoft.com/en-us/java/azure/spring-framework/?view=azure-java-stable). 

You can also visit [Spring Cloud Azure Playground](https://aka.ms/springcloud) to quickly generate a new Spring Cloud applications for Azure.

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

## Code of Conduct 

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Data and Telemetry 

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy statement](https://privacy.microsoft.com/en-us/privacystatement) to learn more. 

To disable this, you can add `spring.cloud.azure.telemetry.enabled=false` in the `application.properties` file. 
