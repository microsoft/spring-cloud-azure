# Spring Cloud Azure

The project provides Spring Cloud integration with Azure services. Developers can use Spring programming model to unlock the power of Azure. It takes care of configurations and offers key features to interact with Azure services, such as Azure Storage and Azure Event Hub. 

## Feature List 

Currently, Spring Cloud Azure offers the following features with samples.  

- [Spring Resource Abstraction for Azure Storage](spring-cloud-azure-storage/) 
- [Spring Cloud Stream Binder for Azure Event Hub](spring-cloud-azure-eventhub-stream-binder/)
- [Spring Cloud Stream with Azure Event Hub Kafka API](spring-cloud-azure-samples/spring-cloud-stream-eventhub-kafka-sample/)
- [Auto-configuration for Azure services with Spring Boot Starters](spring-cloud-azure-starters/)

The following Spring Boot Starters are available in Maven Central Repository. 

Starter Name | Version | Sample
---|---|---
[spring-cloud-azure-starter-storage](spring-cloud-azure-starters/spring-cloud-azure-starter-storage/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-storage.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-storage%22) | [Sample](spring-cloud-azure-samples/spring-cloud-azure-storage-sample)
[spring-cloud-azure-starter-cache](spring-cloud-azure-starters/spring-cloud-azure-starter-cache/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-cache.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-cache%22) | [Sample](spring-cloud-azure-samples/spring-cloud-azure-cache-sample)
[spring-cloud-azure-starter-eventhub](spring-cloud-azure-starters/spring-cloud-azure-starter-eventhub/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-eventhub.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-eventhub%22) | [Sample](spring-cloud-azure-samples/spring-cloud-azure-eventhub-binder-sample)
[spring-cloud-azure-starter-sql-sqlserver](spring-cloud-azure-starters/spring-cloud-azure-starter-sql-sqlserver/) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-cloud-azure-starter-sql-sqlserver.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22spring-cloud-azure-starter-sql-sqlserver%22) | [Sample](spring-cloud-azure-samples/spring-cloud-azure-sql-sample)

## Samples 

Please use the [samples](spring-cloud-azure-samples/) as a reference for how to use Spring Cloud Azure in your projects. 

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
