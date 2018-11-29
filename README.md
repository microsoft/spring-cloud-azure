[![Build Status](https://travis-ci.com/Microsoft/spring-cloud-azure.svg?branch=master)](https://travis-ci.com/Microsoft/spring-cloud-azure)
[![codecov.io](https://codecov.io/gh/Microsoft/spring-cloud-azure/coverage.svg?branch=master)](https://codecov.io/gh/Microsoft/spring-cloud-azure?branch=master)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/Microsoft/spring-cloud-azure/blob/master/LICENCE)

# Spring Cloud Azure

[Spring Cloud](http://projects.spring.io/spring-cloud/) provides boilerplate patterns for developers to quickly build and orchestrate their microservice based applications. Based on that, **Spring Cloud Azure** is designed to provide seamless Spring integration with Azure services. Developers can adopt a Spring-idiomatic way to take advantage of services on Azure, with only few lines of configuration and minimal code changes. 

## Module and Starter

### [Spring Cloud Stream](https://cloud.spring.io/spring-cloud-stream/)
Module | Description | Version | Sample
---|---|--- | ---
[Spring Cloud Stream Binder for Azure Event Hub](spring-cloud-azure-eventhub-stream-binder/) | Binder implementation for Spring Cloud Stream with Azure Event Hub |[![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-eventhub-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-eventhub-stream-binder%22) |[Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-eventhub-binder-sample)
[Spring Cloud Stream with Azure Event Hub Kafka API](spring-cloud-azure-samples/spring-cloud-stream-eventhub-kafka-sample/) | Auto configuration for Kafka Binder with Azure Event Hub |  |[Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-eventhub-kafka-sample)
[Spring Cloud Stream Binder for Azure Service Bus Topic](spring-cloud-azure-servicebus-topic-stream-binder/) | Binder implementation for Spring Cloud Stream with Azure Service Bus Topic | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-servicebus-topic-stream-binder.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-servicebus-topic-stream-binder%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-servicebus-topic-binder-sample)

### [Spring Integration](https://spring.io/projects/spring-integration)
Module | Description | Version | Sample
---|---|--- | ---
[Spring Integration for Azure Event Hub](spring-integration-azure/spring-integration-eventhub) | Event hub template and adapter implementaion for Spring Integration | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-eventhub.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-eventhub%22) | [Integration Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-eventhub-integration-sample) [Template Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-eventhub-operation-sample)
[Spring Integration for Azure Service Bus](spring-integration-azure/spring-integration-servicebus) | Service Bus template and adapter implementaion for Spring Integration | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-servicebus.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-servicebus%22)| [Integration Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-servicebus-integration-sample) [Template Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-servicebus-operation-sample)
[Spring Integration for Storage Queue](spring-integration-azure/spring-integration-storage-queue) | Storage Queue template and adapter implementaion for Spring Integration | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-storage-queue.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-storage-queue%22)| [Integration Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-storage-queue-integration-sample) [Template Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-storage-queue-operation-sample)

### [Spring Resource](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources)

Module | Description | Version | Sample
---|---|--- | ---
[Spring Resource Abstraction for Azure Storage](spring-cloud-azure-storage/) | Implementation for Spring Resource with Azure Storage | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-azure-starter-storage.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-azure-starter-storage%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-storage-sample)

### [Spring Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html)

Module | Description | Version | Sample
---|---|--- | ---
[Spring Caching with Azure Redis Cache](spring-cloud-azure-samples/spring-cloud-azure-cache-sample) | Auto configuration for Spring Caching with Azure Redis Cache | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-azure-starter-cache.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-azure-starter-cache%22) | [Sample](https://github.com/Microsoft/spring-cloud-azure/tree/release/1.0.0.M2/spring-cloud-azure-samples/spring-cloud-azure-cache-sample)

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
