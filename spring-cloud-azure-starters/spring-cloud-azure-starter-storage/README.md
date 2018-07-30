# Spring Cloud Azure Storage 

The project implements Spring Resource abstraction for Azure Storage service which allows you to interact with [Azure Blob storage](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction) using Spring programming model.

## Samples 

Please use this [sample](../../spring-cloud-azure-samples/spring-cloud-azure-storage-sample/) as a reference for how to use **Spring Cloud Azure Storage** in your projects. 

## Feature List 

- [Auto-configuration for Azure Blob storage](#auto-configuration-for-azure-blob-storage)
- [Map with a resource](#map-with-a-resource)
- [Read and write to a resource](#read-and-write-to-a-resource)
- [Other operations](#other-operations) 

### Auto-configuration for Azure Blob storage

We provide a Spring Boot Starter [`spring-cloud-azure-starter-storage`](spring-cloud-azure-starters/spring-cloud-azure-starter-storage/) to auto-configure Azure Storge in your project. 

If you are using Maven, add the following dependency to your project . 

```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-starter-storage</artifactId>
</dependency>
```
If you are using Gradle, add the following dependency to your project. 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-starter-storage'
}
```

The [`spring-cloud-azure-starter-storage`](spring-cloud-azure-starters/spring-cloud-azure-starter-storage/) provides the following configuration options in `application.properties`.

Name | Description | Required | Default 
---|---|---|---
 spring.cloud.azure.credentialFilePath | Location of azure credential file | Yes | 
 spring.cloud.azure.resourceGroup | Name of Azure resource group | Yes | 
 spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
 spring.cloud.azure.storage.account | Name of the Azure Storage Account. Will create a new one if not existing | Yes |

### Map with a resource 
You can use the annotation of `@Value("blob://{containerName}/{blobName}")` to map a `Resource` with that in [Azure Blob Storage](https://azure.microsoft.com/en-us/services/storage/blobs/).

```
@Value("blob://{containerName}/{blobName}")
private Resource blobFile;
```

### Read and write to a resource 
 You can read a resource from Azure Blob storage with `getInputStream()` method.

```
 this.blobFile.getInputStream();
```
You can write to a resource in Azure Blob storage by casting the Spring `Resource` to `WritableResource`. 

```
(WritableResource) this.blobFile).getOutputStream();
```

### Other operations 
The Spring Resource abstraction for Azure Storage also supports [other operations](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources) defined in Spring's `Resource` interface. 


