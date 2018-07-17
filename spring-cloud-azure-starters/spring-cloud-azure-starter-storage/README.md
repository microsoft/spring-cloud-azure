# Spring Cloud Azure Storage 

The project implements Spring Resource abstraction for Azure Storage which allows you to interact with Azure Storage instances using Spring programming model.

## Feature List 

- [Auto-configuration for Azure Storage](#auto-configuration-for-azure-storage) 
- [Read a file from Azure Storage](#read-a-file-from-azure-storage)
- [Write to a file in Azure Storage](#write-to-a-file-in-azure-storage)
- [Other operations](#other-oeprations) 

### Auto-configuration for Azure Storage

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

### Read a file from Azure Storage
You can use the annotation of `@Value("blob://{containerName}/{blobName}")` to map a `Resource` with a file in [Azure Blob Storage](https://azure.microsoft.com/en-us/services/storage/blobs/). The following code snippet shows how to read a file with `getInputStream()` method.

```
@Value("blob://{containerName}/{blobName}")
private Resource blobFile;

@RequestMapping(value = "/", method = RequestMethod.GET)
public String readBlobFile() throws IOException {
    return StreamUtils.copyToString(
            this.blobFile.getInputStream(),
            Charset.defaultCharset()) + "\n";
}
```
### Write to a file in Azure Storage
You can write to a file in Azure Storage by casting the Spring `Resource` to `WritableResource`. 

```
@Value("blob://{containerName}/{blobName}")
private Resource blobFile;

@RequestMapping(value = "/", method = RequestMethod.POST)
    public String writeBlobFile(@RequestBody String data) throws IOException {
    try (OutputStream os = ((WritableResource) this.blobFile).getOutputStream()) {
        os.write(data.getBytes());
    }
    return "file was updated\n";
}
```

### Other operations 
The Spring Resource abstraction for Azure Storage also supports [other operations](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources) defined in Spring's `Resource` interface. 

## Samples 

Please use this [sample](spring-cloud-azure-samples/spring-cloud-azure-storage-sample/) as a reference for how to use **Spring Cloud Azure Storage** in your projects. 
