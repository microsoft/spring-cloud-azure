---
title: How to use the Spring Boot Starter for Azure Storage
description: Learn how to configure a Spring Boot Initializer app with the Azure Storage starter.
services: storage
documentationcenter: java
ms.date: 03/30/2022
ms.service: storage
ms.topic: article
ms.workload: storage
ms.custom: devx-track-java, devx-track-azurecli
---

# How to use the Spring Boot Starter for Azure Storage

This article walks you through creating a custom application using the **Spring Initializr**, then adding the Azure Storage Blob starter to your application, and then using your application to upload a blob to your Azure storage account.

## Prerequisites

The following prerequisites are required in order to follow the steps in this article:

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/pricing/free-trial/).
* The [Azure Command-Line Interface (CLI)](/cli/azure/index).
* A supported Java Development Kit (JDK). For more information about the JDKs available for use when developing on Azure, see [Java support on Azure and Azure Stack](../fundamentals/java-support-on-azure.md).
* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

> [!IMPORTANT]
> Spring Boot version 2.5 or 2.6 is required to complete the steps in this article.

## Create an Azure Storage Account and blob container for your application

The following procedure creates an Azure storage account and container in the portal.

1. Browse to the Azure portal at <https://portal.azure.com/> and sign in.

1. Select **Create a resource**, then **Get started**, and then select **Storage Account**.

   ![Azure portal, create a resource, search for storage accounts.][IMG01]

1. On the **Create storage account** page, enter the following information:

   * Select **Subscription**.
   * Select **Resource group**, or create a new resource group.
   * Enter a unique **Storage account name**, which will become part of the URI for your storage account. For example: if you entered **wingtiptoysstorage** for the **Name**, the URI would be *wingtiptoysstorage.core.windows.net*.
   * Specify the **Location** for your storage account.
1. When you have specified the options listed above, select **Review + create**.

   ![Azure portal, create a storage account.][IMG01-01]

1. Review the specification, then select **Create** to create your storage account.
1. When the deployment is complete, select **Go to resource**.
1. Select **Containers**.
1. Select **Container**.
   * Name the container.
   * Select *Blob* from the drop-down list.

   ![Azure portal, storage account, containers, new container pane.][IMG02]

1. The Azure portal will list your blob container after is has been created.

You can also use Azure CLI to create an Azure storage account and container using the following steps. Remember to replace the placeholder values (in angle brackets) with your own values.

1. Open a command prompt.

1. Sign in to your Azure account:

   ```azurecli
   az login
   ```

1. If you don't have a resource group, create one using the following command:

   ```azurecli
   az group create \
      --name <resource-group> \
      --location <location>
   ```

1. Create a storage account by using the following command:

   ```azurecli
    az storage account create \
      --name <storage-account-name> \
      --resource-group <resource-group> \
      --location <location>
   ```

1. To create a container, use the following command:

   ```azurecli
    az storage container create \
      --account-name <storage-account-name> \
      --name <container-name> \
      --auth-mode login
   ```

## Create a simple Spring Boot application with the Spring Initializr

The following procedure creates the Spring boot application.

1. Browse to <https://start.spring.io/>.

1. Specify the following options:

   * Generate a **Maven** project.
   * Specify **Java 11**.
   * Specify a **Spring Boot** version that is equal to **2.5.10**.
   * Specify the **Group** and **Artifact** names for your application.
   * Add the **Spring Web** dependency.

      >[!div class="mx-imgBorder"]
      >![Basic Spring Initializr options][SI01]

   > [!NOTE]
   > The Spring Initializr uses the **Group** and **Artifact** names to create the package name; for example: *com.wingtiptoys.storage*.

1. When you have specified the options listed above, select **GENERATE**.

1. When prompted, download the project to a path on your local computer.

1. After you have extracted the files on your local system, your simple Spring Boot application will be ready to edit.

## Configure your Spring Boot app to use the Azure Storage Blob starter

### Add dependency in pom.xml

The following procedure configures the Spring boot application to use Azure storage.

1. Locate the *pom.xml* file in the root directory of your app; for example:

   `C:\SpringBoot\storage\pom.xml`

   -or-

   `/users/example/home/storage/pom.xml`

1. Open the *pom.xml* file in a text editor, and add the Spring Cloud Azure Storage starter to the list of `<dependencies>`:

   ```xml
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>spring-cloud-azure-starter-storage-blob</artifactId>
      <version>4.0.0</version>
    </dependency>
   ```

1. Save and close the *pom.xml* file.

### Configure property in application.yml

The following procedure configures the Spring boot application to use your Azure storage account.

1. Locate the *application.yml* in the *resources* directory of your app; for example:

   `C:\SpringBoot\storage\src\main\resources\application.yml`

   -or-

   `/users/example/home/storage/src/main/resources/application.yml`

2. Open the *application.yml* file in a text editor, add the following lines, and then replace the sample values with the appropriate properties for your storage account:

```yaml
spring:
  cloud:
    azure:
      storage:
        blob:
          account-name: [storage-account-name]
          account-key: [storage-account-access-key]
          endpoint: [storage-blob-service-endpoint]
```

   Where:

   | Name                        | Description                                         | Required                                       |
   |-----------------------------|-----------------------------------------------------|------------------------------------------------|
   | spring.cloud.azure.storage.blob.account-name   | The name of the Azure Storage account.              | Yes                                            |
   | spring.cloud.azure.storage.blob.account-key    | The access key of the Azure Storage account.        | Yes                                            |
   | spring.cloud.azure.storage.blob.endpoint       | The blob endpoint URL of the Azure Storage account. | Yes|
   

3. Save and close the *application.yml* file.

## Add sample code to implement basic Azure storage functionality

In this section, you will create the necessary Java classes for storing a blob in your Azure storage account.

### Add a blob controller class

1. Create a new Java file named *BlobController.java* in the package directory of your app; for example:

   `C:\SpringBoot\storage\src\main\java\com\wingtiptoys\storage\BlobController.java`

   -or-

   `/users/example/home/storage/src/main/java/com/wingtiptoys/storage/BlobController.java`

1. Open `BlobController.java` in a text editor, and add the following lines to the file. Replace the *`<your-resource-group>`*, *`<your-artifact-name>`*, *`<your-container-name>`*, and *`<your-blob-name>`* placeholders with your values.

   ```java
   package com.<your-resource-group>.<your-artifact-name>;

   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.core.io.Resource;
   import org.springframework.core.io.WritableResource;
   import org.springframework.util.StreamUtils;
   import org.springframework.web.bind.annotation.*;

   import java.io.IOException;
   import java.io.OutputStream;
   import java.nio.charset.Charset;

   @RestController
   @RequestMapping("blob")
   public class BlobController {

       @Value("azure-blob://<your-container-name>/<your-blob-name>")
       private Resource blobFile;

       @GetMapping("/readBlobFile")
       public String readBlobFile() throws IOException {
           return StreamUtils.copyToString(
                   this.blobFile.getInputStream(),
                   Charset.defaultCharset());
       }

       @PostMapping("/writeBlobFile")
       public String writeBlobFile(@RequestBody String data) throws IOException {
           try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
               os.write(data.getBytes());
           }
           return "file was updated";
       }
   }
   ```

1. Save and close the blob controller Java file.

1. Open a command prompt and change directory to the folder where your *pom.xml* file is located; for example:

   ```cmd
   cd C:\SpringBoot\storage
   ```

   -or-

   ```bash
   cd /users/example/home/storage
   ```

1. Build your Spring Boot application with Maven and run it; for example:

   ```shell
   mvn clean package
   mvn spring-boot:run
   ```

1. Once your application is running, you can use *curl* to test your application; for example:

   a. Send a POST request to update a file's contents:

      ```shell
      curl http://localhost:8080/blob/writeBlobFile -d "new message" -H "Content-Type: text/plain"
      ```

      You should see a response that  `file was updated`.

   b. Send a GET request to verify the file's contents:

      ```shell
      curl -X GET http://localhost:8080/blob/readBlobFile
      ```

     You should see the "new message" text that you posted.

## Summary

In this tutorial, you created a new Java application using the **Spring Initializr**, added the Azure Storage Blob starter to your application, and then configured your application to upload a blob to your Azure storage account.

## Clean up resources

When no longer needed, use the [Azure portal](https://portal.azure.com/) to delete the resources created in this article to avoid unexpected charges.

## Next steps

To learn more about Spring and Azure, continue to the Spring on Azure documentation center.

> [!div class="nextstepaction"]
> [Spring on Azure](index.yml)

### Additional Resources

For more information about the additional Spring Boot Starters that are available for Microsoft Azure, see [Spring Boot Starters for Azure](spring-boot-starters-for-azure.md).

For detailed information about additional Azure storage APIs that you can call from your Spring Boot applications, see the following articles:

* [How to use Azure Blob storage from Java](/azure/storage/blobs/storage-java-how-to-use-blob-storage)
* [How to use Azure Queue storage from Java](/azure/storage/queues/storage-java-how-to-use-queue-storage)
* [How to use Azure Table storage from Java](/azure/cosmos-db/table-storage-how-to-use-java)
* [How to use Azure File storage from Java](/azure/storage/files/storage-java-how-to-use-file-storage)

<!-- IMG List -->

[IMG01]: media/configure-spring-boot-starter-java-app-with-azure-storage/create-storage-account-01.png
[IMG01-01]: media/configure-spring-boot-starter-java-app-with-azure-storage/create-storage-account-01-01.png
[IMG02]: media/configure-spring-boot-starter-java-app-with-azure-storage/create-storage-account-02.png

[SI01]: media/spring-initializer/2.5.10/mvn-java8-storage-web.png