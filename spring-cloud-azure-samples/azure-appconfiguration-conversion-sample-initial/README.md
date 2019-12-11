# Spring Cloud Azure Config Conversion Sample

This sample shows how to convert a Spring Cloud Application with Cosmos DB to be using App Configuration + Key Vault

## Prerequisite

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/free/).

* A [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable), version 8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

## Quick Start

### Create an Azure Cosmos DB on Azure

1. Use the Azure CLI [az cosmosdb create](https://docs.microsoft.com/cli/azure/cosmosdb?view=azure-cli-latest#az-cosmosdb-create).

    ```azurecli
    az cosmosdb create --name my-cosmos-db --resource-group MyResourceGroup
    ```

    This operation will return json, among them is a documentEndpoint, record this.

    ```azurecli
    {
      ...
      "documentEndpoint": "https://my-cosmos.documents.azure.com:443/",
      ...
    }
    ```

1. Then use the [az cosmosdb keys list](https://docs.microsoft.com/cli/azure/cosmosdb/keys?view=azure-cli-latest#az-cosmosdb-keys-list).

    ```azurecli
        az cosmosdb keys list --name my-cosmos-db -g MyResourceGroup
    ```

    Record the primaryMasterKey.

    ```azurecli
    {
      "primaryMasterKey": "...",
      "primaryReadonlyMasterKey": "...",
      "secondaryMasterKey": "...",
      "secondaryReadonlyMasterKey": "..."
    }
    ```

### Clone the sample Project

In this section, you clone a containerized Spring Boot application and test it locally.

1. Open a command prompt or terminal window and create a local directory to hold your Spring Boot application, and change to that directory; for example:

   ```shell
   md C:\SpringBoot
   cd C:\SpringBoot
   ```

   -- or --

   ```shell
   md /users/robert/SpringBoot
   cd /users/robert/SpringBoot
   ```

1. Clone the [Spring Boot on Docker Getting Started] sample project into the directory you created; for example:

   ```shell
   git clone https://github.com/microsoft/spring-cloud-azure.git
   ```

1. Change directory to the initial project; for example:

   ```shell
   cd spring-cloud-azure/spring-cloud-azure-samples/azure-appconfiguration-conversion-sample/initial
   ```

### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.

1. Replace below properties in `application.properties` with information from your database.

   ```properties
   azure.cosmosdb.uri=your-cosmosdb-uri
   azure.cosmosdb.key=your-cosmosdb-key
   azure.cosmosdb.database=your-cosmosdb-databasename

   ```

### Run the sample

1. Build the JAR file using Maven; for example:

   ```shell
   mvn clean package
   ```

1. When the web app has been created, start the web app using Maven; for example:

   ```shell
   mvn spring-boot:run
   ```

1. View the results in the console.

1. You should see the following message displayed: **findOne in User collection get result: testFirstName**

### Convert to Using App Configuration

1. Use the Azure CLI [az keyvault create](https://docs.microsoft.com/cli/azure/cosmosdb?view=azure-cli-latest#az-cosmosdb-create)

    ```azurecli
    az keyvault create --name myVaultName -g MyResourceGroup
    ```

1. Use the Azure CLI [az keyvault create](https://docs.microsoft.com/cli/azure/appconfig?view=azure-cli-latest#az-appconfig-create)

    ```azurecli
    az appconfig create --name myConfigStoreName -g MyResourceGroup -l eastus
    ```

1. For this tutorial, you'll use a service principal for authentication to KeyVault. To create this service principal, use the Azure CLI [az ad sp create-for-rbac](/cli/azure/ad/sp?view=azure-cli-latest#az-ad-sp-create-for-rbac) command:

    ```azurecli
    az ad sp create-for-rbac -n "<unique uri>" --sdk-auth
    ```

    This operation will return a series of key / value pairs.

    ```console
    {
    "clientId": "iiiiiiii-iiii-iiii-iiii-iiiiiiiiiiii",
    "clientSecret": "ssssssss-ssss-ssss-ssss-sssssssssss",
    "subscriptionId": "bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb",
    "tenantId": "ttttttt-tttt-tttt-tttt-ttttttttttt",
    "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
    "resourceManagerEndpointUrl": "https://management.azure.com/",
    "activeDirectoryGraphResourceId": "https://graph.windows.net/",
    "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
    "galleryEndpointUrl": "https://gallery.azure.com/",
    "managementEndpointUrl": "https://management.core.windows.net/"
    }
    ```

1. Run the following command to allow the service principal to access your key vault:

    ```azurecli
        az keyvault set-policy -n myVaultName --spn "iiiiiiii-iiii-iiii-iiii-iiiiiiiiiiii" --secret-permissions get
    ```

1. Upload your Cosmos DB key to Key Vault.

    ```azurecli
        az keyvault secret set --vault-name myVaultName --name "COSMOSDB-KEY" --value yourCosmosDBKey
    ```

1. Upload your Configurations Cosmos DB name and URI to App Configuration

    ```azurecli
        az appconfig kv set --name myConfigStoreName --key "/application/azure.cosmosdb.database" --value myCosmosDBName --content-type " " --yes
        az appconfig kv set --name myConfigStoreName --key "/application/azure.cosmosdb.uri" --value myCosmosDBUri --content-type " " --yes
    ```

1. Add a Key Vault Reference to App Configuration, make sure to update the uri with your config store name.

    ```azurecli
        az appconfig kv set --name myConfigStoreName --key "/application/azure.cosmosdb.key" --value "{\"uri\":\"https://myVaultName.vault.azure.net/secrets/COSMOSDB-KEY\"}" --content-type "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8" --yes
    ```

1. Get a connection string to App Configuration.

    ```azurecli
        az appconfig credential list -g MyResourceGroup --name myConfigStoreName
    ```

1. Delete `application.propertes` from `src/main/resources`.

1. Create a new file called `bootstrap.properties` in `src/main/resources`, and add the following.

    ```properties
        spring.cloud.azure.appconfiguration.stores[0].connection-string=${CONFIG_STORE_CONNECTION_STRING}

    ```

1. Update the pom.xml file to now include.

    ```xml
    <dependency>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>spring-cloud-starter-azure-appconfiguration-config</artifactId>
        <version>1.1.0.M4</version>
    </dependency>
    ```

1. Create the following Environment Variables with their respective values: AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, AZURE_TENANT_ID, CONFIG_STORE_CONNECTION_STRING.

### Run the updated sample

1. Build the JAR file using Maven; for example:

   ```shell
   mvn clean package
   ```

1. When the web app has been created, start the web app using Maven; for example:

   ```shell
   mvn spring-boot:run
   ```

1. View the results in the console.

1. You should see the following message displayed: **findOne in User collection get result: testFirstName**
