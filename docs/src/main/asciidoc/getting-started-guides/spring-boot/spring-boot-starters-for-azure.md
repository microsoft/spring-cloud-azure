---
title: Spring Boot Starters for Azure
description: This article describes the various Spring Boot Starters that are available for Azure.
documentationcenter: java
ms.date: 03/30/2022
ms.service: multiple
ms.tgt_pltfrm: multiple
ms.topic: article
ms.custom: devx-track-java
---

# Spring Boot Starters for Azure

This article describes the various Spring Boot Starters for the [Spring Initializr] that provide Java developers with integration features for working with Microsoft Azure.

>[!div class="mx-imgBorder"]
![Configure Azure Spring Boot Starters with Initializr][configure-azure-spring-boot-starters-with-initializr]

The following Spring Boot Starters are currently available for Azure:

* **[Azure Support](#azure-support)**

   Provides auto-configuration support for Azure Services; e.g. Service Bus, Storage, Active Directory, etc.

* **[Azure Active Directory](#azure-active-directory)**

   Provides integration support for Spring Security with Azure Active Directory for authentication.

* **[Azure Key Vault](#azure-key-vault)**

   Provides Spring value annotation support for integration with Azure Key Vault Secrets.

* **[Azure Storage](#azure-storage)**

   Provides Spring Boot support for Azure Storage services.

<a name="azure-support"></a>
## Azure Support

This Spring Boot Starter provides auto-configuration support for Azure Services; for example: Service Bus, Storage, Active Directory, Cosmos DB, Key Vault, etc.

For examples of how to use the various Azure features that are provided by this starter, see the following:

* The [azure-spring-boot-samples](https://github.com/Azure-Samples/azure-spring-boot-samples) repo on GitHub.

When you add this starter to a Spring Boot project, the following changes are made to the *pom.xml* file:

* The following property is added to `<properties>` element:

   ```xml
   <properties>
      <!-- Other properties will be listed here -->
      <java.version>1.8</java.version>
      <version.spring.cloud.azure>4.0.0</version.spring.cloud.azure>
   </properties>
   ```

* The default `spring-boot-starter` dependency is replaced with the following:

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-starter</artifactId>
        </dependency>
    </dependencies>
    ```

* The following section is added to the file:

   ```xml
   <dependencyManagement>
      <dependencies>
         <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-dependencies</artifactId>
            <version>${version.spring.cloud.azure}</version>
            <type>pom</type>
            <scope>import</scope>
         </dependency>
      </dependencies>
   </dependencyManagement>
   ```

* For more information about using this bom, see [reference doc](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#setting-up-dependencies).

<a name="azure-active-directory"></a>
## Azure Active Directory

This Spring Boot Starter provides auto-configuration support for Spring Security in order to provide integration with Azure Active Directory for authentication.

For examples of how to use the Azure Active Directory features that are provided by this starter, see the following:

* The [spring-cloud-azure-starter-active-directory samples](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0/aad/spring-cloud-azure-starter-active-directory) repo on GitHub.

When you add this starter to a Spring Boot project, the following changes are made to the *pom.xml* file:

* The following property is added to `<properties>` element:

   ```xml
   <properties>
      <!-- Other properties will be listed here -->
      <java.version>1.8</java.version>
      <version.spring.cloud.azure>4.0.0</version.spring.cloud.azure>
   </properties>
   ```

* The default `spring-boot-starter` dependency is replaced with the following:

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-starter-active-directory</artifactId>
        </dependency>
    </dependencies>
    ```

* The following section is added to the file:

   ```xml
   <dependencyManagement>
      <dependencies>
         <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-dependencies</artifactId>
            <version>${version.spring.cloud.azure}</version>
            <type>pom</type>
            <scope>import</scope>
         </dependency>
      </dependencies>
   </dependencyManagement>
   ```

* For more information about using this starter, see [Spring Security Support](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#spring-security-support).

## Azure Key Vault

This Spring Boot Starter provides Spring value annotation support for integration with Azure Key Vault Secrets.

For examples of how to use the Azure Key Vault features that are provided by this starter, see the following:

* [spring-cloud-azure-starter-keyvault-secrets samples](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0/keyvault/spring-cloud-azure-starter-keyvault-secrets).

When you add this starter to a Spring Boot project, the following changes are made to the *pom.xml* file:

* The following property is added to `<properties>` element:

   ```xml
   <properties>
      <!-- Other properties will be listed here -->
      <java.version>1.8</java.version>
      <version.spring.cloud.azure>4.0.0</version.spring.cloud.azure>
   </properties>
   ```

* The default `spring-boot-starter` dependency is replaced with the following:

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
        </dependency>
    </dependencies>
    ```

* The following section is added to the file:

   ```xml
   <dependencyManagement>
      <dependencies>
         <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-dependencies</artifactId>
            <version>${version.spring.cloud.azure}</version>
            <type>pom</type>
            <scope>import</scope>
         </dependency>
      </dependencies>
   </dependencyManagement>
   ```

* For more information about using this starter, see [Secret Management](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#secret-management).

<a name="azure-storage"></a>
## Azure Storage

This Spring Boot Starter provides Spring Boot integration support for Azure Storage services.

For examples of how to use the Azure Storage features that are provided by this starter, see the following:

* [How to use the Spring Boot Starter for Azure Storage](configure-spring-boot-starter-java-app-with-azure-storage.md)
* [spring-cloud-azure-starter-integration-storage-queue samples](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0/storage/spring-cloud-azure-starter-integration-storage-queue)

When you add this starter to a Spring Boot project, the following changes are made to the *pom.xml* file:

* The following property is added to `<properties>` element:

   ```xml
   <properties>
      <!-- Other properties will be listed here -->
      <java.version>1.8</java.version>
      <version.spring.cloud.azure>4.0.0</version.spring.cloud.azure>
   </properties>
   ```

* The default `spring-boot-starter` dependency is replaced with the following:

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-starter-integration-storage-queue</artifactId>
        </dependency>
    </dependencies>
    ```

* The following section is added to the file:

   ```xml
   <dependencyManagement>
      <dependencies>
         <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-dependencies</artifactId>
            <version>${version.spring.cloud.azure}</version>
            <type>pom</type>
            <scope>import</scope>
         </dependency>
      </dependencies>
   </dependencyManagement>
   ```

* For more information about using this starter, see [Spring Integration with Azure Storage Queue](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#spring-integration-with-azure-storage-queue).

## Application Insights

Azure Monitor Application Insights can help you understand how your app is performing and how it's being used. Application Insights uses the Java agent to enable the application monitor. There are no code changes needed, and you can enable the Java agent with just a couple of configuration changes. For instructions and more information, see [Java codeless application monitoring Azure Monitor Application Insights](/azure/azure-monitor/app/java-in-process-agent#configuration-options).

## Next steps

To learn more about Spring and Azure, continue to the Spring on Azure documentation center.

> [!div class="nextstepaction"]
> [Spring on Azure](./index.yml)

### Additional Resources

For more information about using [Spring Boot] applications on Azure, see [Spring on Azure].

For more information about using Azure with Java, see the [Azure for Java Developers] and the [Working with Azure DevOps and Java].

For help with getting started with your own Spring Boot applications, see the **Spring Initializr** at https://start.spring.io/.

<!-- URL List -->

[Azure for Java Developers]: ../index.yml
[Working with Azure DevOps and Java]: /azure/devops/
[Spring Boot]: http://projects.spring.io/spring-boot/
[Spring on Azure]: ./index.yml
[Spring Framework]: https://spring.io/
[Spring Initializr]: https://start.spring.io/

<!-- IMG List -->

[configure-azure-spring-boot-starters-with-initializr]: media/spring-boot-starters-for-azure/configure-azure-spring-boot-starters-with-initializr.png
