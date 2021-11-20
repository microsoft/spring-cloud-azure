## Getting Started

### Setting up Dependencies

#### Bill of Material (BOM)
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>spring-cloud-azure-dependencies</artifactId>
      <version>4.0.0-beta.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

#### Starter Dependencies

Spring Cloud Azure Starters are a set of convenient dependency descriptors to include in your application. Each starter contains all the dependencies and transitive dependencies needed to begin using their corresponding Spring Cloud Azure module. It boosts your Spring Boot application development with Azure services.

For example, if you want to get started using Spring and Azure Cosmos DB for data persistence, include the `spring-cloud-azure-starter-cosmos` dependency in your project.

The following application starters are provided by Spring Cloud Azure under the `com.azure.spring` group:

| Name                                            | Description                                                  |
| ----------------------------------------------- | ------------------------------------------------------------ |
| spring-cloud-azure-starter                      | Core starter, including auto-configuration support           |
| spring-cloud-azure-starter-active-directory     | Starter for using Azure Active Directory with Spring Security |
| spring-cloud-azure-starter-active-directory-b2c | Starter for using Azure Active Directory B2C with Spring Security |
| spring-cloud-azure-starter-appconfiguration     | Starter for using Azure App Configuration                    |
| spring-cloud-azure-starter-cosmos               | Starter for using Azure Cosmos DB                            |
| spring-cloud-azure-starter-eventhubs            | Starter for using Azure Event Hubs                           |
| spring-cloud-azure-starter-keyvault-secrets     | Starter for using Azure Key Vault Secrets                    |
| spring-cloud-azure-starter-servicebus           | Starter for using Azure Service Bus                          |
| spring-cloud-azure-starter-servicebus-jms       | Starter for using Azure Service Bus and JMS                  |
| spring-cloud-azure-starter-storage-blob         | Starter for using Azure Storage Blob                         |
| spring-cloud-azure-starter-storage-file-share   | Starter for using Azure Storage File Share                   |
| spring-cloud-azure-starter-storage-queue        | Starter for using Azure Storage Queue                        |
| spring-cloud-azure-starter-actuator             | Starter for using Spring Boot’s Actuator which provides production ready features |

Below are starters for **Spring Data** support:

| Name                                   | Description                                                 |
| -------------------------------------- | ----------------------------------------------------------- |
| spring-cloud-azure-starter-data-cosmos | Starter for using Azure Cosmos DB and Spring Data Cosmos DB |

Below are starters for **Spring Integration** support:

| Name                                                 | Description                                                  |      |
| ---------------------------------------------------- | ------------------------------------------------------------ | ---- |
| spring-cloud-azure-starter-integration-eventhubs     | Starter for using Azure Event Hubs and Spring Integration    |      |
| spring-cloud-azure-starter-integration-servicebus    | Starter for using Azure Service Bus and Spring Integration   |      |
| spring-cloud-azure-starter-integration-storage-queue | Starter for using Azure Storage Queue and Spring Integration |      |

Below are starters for **Spring Cloud Stream** support:

| Name                                         | Description                                                  |
| -------------------------------------------- | ------------------------------------------------------------ |
| spring-cloud-azure-starter-stream-eventhubs  | Starter for using Azure Event Hubs and Spring Cloud Stream Binder |
| spring-cloud-azure-starter-stream-servicebus | Starter for using Azure Service Bus and Spring Cloud Stream Binder |

### Learning Spring Cloud Azure

#### Sample Applications

https://github.com/Azure-Samples/azure-spring-boot-samples