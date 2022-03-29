---
title: "Tutorial: Read a secret from Azure Key Vault in a Spring Boot application"
description: In this tutorial, you create a Spring Boot app that reads a value from Azure Key Vault, and you deploy the app to Azure App Service and Azure Spring Cloud.
ms.date: 03/30/2022
ms.service: key-vault
ms.topic: tutorial
ms.custom: devx-track-java, devx-track-azurecli
ms.author: edburns
---

# Tutorial: Read a secret from Azure Key Vault in a Spring Boot application

This tutorial shows you how to create a Spring Boot app that reads a value from Azure Key Vault. After creating the app, you'll deploy it to Azure App Service and Azure Spring Cloud.

Spring Boot applications externalize sensitive information such as usernames and passwords. Externalizing sensitive information enables better maintainability, testability, and security. Storing secrets outside of the code is better than hard coding the information, or inlining it at build time.

In this tutorial, you learn how to:

> [!div class="checklist"]
> * Create an Azure Key Vault and store a secret
> * Create an app with Spring Initializr
> * Add Key Vault integration to the app
> * Deploy to Azure App Service
> * Redeploy to Azure App Service with managed identities for Azure resources
> * Deploy to Azure Spring Cloud

## Prerequisites

- [!INCLUDE [free subscription](includes/quickstarts-free-trial-note.md)]
[!INCLUDE [curl](includes/prerequisites-curl.md)]
[!INCLUDE [jq](includes/prerequisites-jq.md)]
[!INCLUDE [Azure CLI](includes/prerequisites-azure-cli.md)]
[!INCLUDE [JDK](includes/prerequisites-java.md)]
[!INCLUDE [Maven](includes/prerequisites-maven.md)]

> [!IMPORTANT]
> Spring Boot version 2.5 or 2.6 is required to complete the steps in this article.

## Create a new Azure Key Vault

The following sections show you how to sign in to Azure and create an Azure Key Vault.

### Sign in to Azure and set your subscription

First, use the following steps to authenticate using the Azure CLI.

1. Optionally, sign out and delete some authentication files to remove any lingering credentials:

   ```azurecli
   az logout
   rm ~/.azure/accessTokens.json
   rm ~/.azure/azureProfile.json
   ```

1. Sign in by using the Azure CLI:

   ```azurecli
   az login
   ```

   Follow the instructions to complete the sign-in process.

1. List your subscriptions:

   ```azurecli
   az account list
   ```

   Azure will return a list of your subscriptions. Copy the `id` value for the subscription that you want to use; for example:

   ```json
   [
     {
       "cloudName": "AzureCloud",
       "id": "ssssssss-ssss-ssss-ssss-ssssssssssss",
       "name": "Converted Windows Azure MSDN - Visual Studio Ultimate",
       "state": "Enabled",
       "tenantId": "tttttttt-tttt-tttt-tttt-tttttttttttt",
       "user": {
         "name": "contoso@microsoft.com",
         "type": "user"
       }
     }
   ]
   ```

1. Specify the GUID for the subscription you want to use with Azure; for example:

   ```azurecli
   az account set -s ssssssss-ssss-ssss-ssss-ssssssssssss
   ```

### Create a service principal

Azure AD *service principals* provide access to Azure resources within your subscription. You can think of a service principal as a user identity for a service. "Service" is any application, service, or platform that needs to access Azure resources. You can configure a service principal with access rights scoped only to those resources you specify. Then, configure your application or service to use the service principal's credentials to access those resources.

To create a service principal, use the following command.

```azurecli
az ad sp create-for-rbac --name contososp --role Contributor
```

> [!NOTE]
> The value of the `--name` option must be unique within the Azure subscription. If you see an error log similar to `Found an existing instance of "...", We will patch it. Insufficient privileges to complete operation`, that means the `name` value already exist in your subscription. Try another name.

Save aside the values returned from the command for use later in the tutorial. The return JSON will look similar to the following output:

```output
{
  "appId": "sample-app-id",
  "displayName": "contososp",
  "name": "http://contososp",
  "password": "sample-password",
  "tenant": "sample-tenant"
}
```

### Create the Key Vault instance

To create and initialize the Azure Key Vault, use the following steps:

1. Determine which Azure region will hold your resources.
   1. To see the list of regions and their locations, see [Azure geographies](https://azure.microsoft.com/regions/).
   1. Use the `az account list-locations` command to find the correct `Name` for your chosen region.

      ```azurecli
      az account list-locations --output table
      ```

      This tutorial uses `eastus`.

1. Create a resource group to hold the Key Vault and the App Service app. The value must be unique within the Azure subscription. This tutorial uses `contosorg`.

   ```azurecli
   az group create --name contosorg --location eastus
   ```

1. Create a new Key Vault in the resource group.

   ```azurecli
   az keyvault create \
       --resource-group contosorg \
       --name contosokv \
       --enabled-for-deployment true \
       --enabled-for-disk-encryption true \
       --enabled-for-template-deployment true \
       --location eastus \
       --query properties.vaultUri \
       --sku standard
   ```

   > [!NOTE]
   > The value of the `--name` option must be unique within the Azure subscription. If you see an error log similar to `Found an existing instance of "...", We will patch it. Insufficient privileges to complete operation`, that means the `name` value already exist in your subscription. Try another name.

   This table explains the options shown above.

   | Parameter | Description |
   |---|---|
   | `enabled-for-deployment` | Specifies the [Key Vault deployment option](/cli/azure/keyvault). |
   | `enabled-for-disk-encryption` | Specifies the [Key Vault encryption option](/cli/azure/keyvault). |
   | `enabled-for-template-deployment` | Specifies the [Key Vault encryption option](/cli/azure/keyvault). |
   | `location` | Specifies the [Azure region](https://azure.microsoft.com/regions/) where your resource group will be hosted. |
   | `name` | Specifies a unique name for your Key Vault. |
   | `query` | Retrieve the Key Vault URI from the response. You need the URI to complete this tutorial. |
   | `sku` | Specifies the [Key Vault SKU option](/cli/azure/keyvault). |

   The Azure CLI will display the URI for Key Vault, which you'll use later; for example:

   ```output
   "https://contosokv.vault.azure.net/"
   ```

1. Configure the Key Vault to allow `get` and `list` operations from that managed identity. The value of the `object-id` is the `appId` from the `az ad sp create-for-rbac` command above.

   ```azurecli
   az keyvault set-policy --name contosokv --spn http://contososp --secret-permissions get list
   ```

   The output will be a JSON object full of information about the Key Vault. It will have a `type` entry with value `Microsoft.KeyVault/vaults`.

   This table explains the properties shown above.

   | Parameter | Description |
   |---|---|
   | name | The name of the Key Vault. |
   | spn | The `name` from the output of `az ad sp create-for-rbac` command above. |
   | secret-permissions | The list of operations to allow from the named principal. |

   > [!NOTE]
   > While the principle of least privilege recommends granting the smallest possible set of privileges to a resource, the design of the Key Vault integration requires at least `get` and `list`.

1. Store a secret in your new Key Vault. A common use case is to store a JDBC connection string. For example:

   ```azurecli
   az keyvault secret set --name "connectionString" \
       --vault-name "contosokv" \
       --value "jdbc:sqlserver://SERVER.database.windows.net:1433;database=DATABASE;"
   ```

   This table explains the options shown above.

   | Parameter | Description |
   |---|---|
   | `name` | Specifies the name of your secret. |
   | `value` | Specifies the value of your secret. |
   | `vault-name` | Specifies your Key Vault name from earlier. |

   The Azure CLI will display the results of your secret creation; for example:

   ```output
   {
     "attributes": {
       "created": "2020-08-24T21:48:09+00:00",
       "enabled": true,
       "expires": null,
       "notBefore": null,
       "recoveryLevel": "Purgeable",
       "updated": "2020-08-24T21:48:09+00:00"
     },
     "contentType": null,
     "id": "https://contosokv.vault.azure.net/secrets/connectionString/sample-id",
     "kid": null,
     "managed": null,
     "tags": {
       "file-encoding": "utf-8"
     },
     "value": "jdbc:sqlserver://.database.windows.net:1433;database=DATABASE;"
   }
   ```

Now that you've created a Key Vault and stored a secret, the next section will show you how to create an app with Spring Initializr.

## Create the app with Spring Initializr

This section shows how to use Spring Initializr to create and run a Spring Boot web application with key vault secrets included.

1. Browse to <https://start.spring.io/>.
1. Select the choices as shown in the picture following this list.
   * **Project**: **Maven Project**
   * **Language**: **Java**
   * **Spring Boot**: **2.5.10**
   * **Group**: *com.contoso* (You can put any valid Java package name here.)
   * **Artifact**: *keyvault* (You can put any valid Java class name here.)
   * **Packaging**: **Jar**
   * **Java**: **11** (You can choose 8, but this tutorial was validated with 11.)
1. Select **Add Dependencies...**.
1. In the text field, type *Spring Web* and press Ctrl+Enter.
1. In the text field type *Azure Key Vault* and press Enter. Your screen should look like the following.

   ![Basic Spring Initializr options][SI01]
1. At the bottom of the page, select **Generate**.
1. When prompted, download the project to a path on your local computer. This tutorial uses a *keyvault* directory in the current user's home directory. The values above will give you a *keyvault.zip* file in that directory.

Use the following steps to examine the application and run it locally.

1. Unzip the *keyvault.zip* file. The file layout will look like the following. This tutorial ignores the *test* directory and its contents.

   ```
   ├── HELP.md
   ├── mvnw
   ├── mvnw.cmd
   ├── pom.xml
   └── src
       ├── main
       │   ├── java
       │   │   └── com
       │   │       └── contoso
       │   │           └── keyvault
       │   │               └── KeyvaultApplication.java
       │   └── resources
       │       ├── application.properties
       │       ├── static
       │       └── templates
   ```

1. Open the *KeyvaultApplication.java* file in a text editor. Edit the file so that it has the following contents.

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;

   @SpringBootApplication
   @RestController
   public class KeyvaultApplication {

       public static void main(String[] args) {
           SpringApplication.run(KeyvaultApplication.class, args);
       }

       @GetMapping("get")
       public String get() {
           return connectionString;
       }

       private String connectionString = "defaultValue\n";

       public void run(String... varl) throws Exception {
           System.out.println(String.format("\nConnection String stored in Azure Key Vault:\n%s\n",connectionString));
       }

   }
   ```

   The following list highlights some details about this code:

   * The class is annotated with `@RestController`. `@RestController` tells Spring Boot that the class can respond to RESTful HTTP requests.
   * The class has a method annotated with `@GetMapping(get)`. `@GetMapping` tells Spring Boot to send HTTP requests with the path `/get` to that method, allowing the response from that method to be returned to the HTTP client.
   * The class has a private instance variable `connectionString`. The value of this instance variable is returned from the `get()` method.

1. Open a Bash window and navigate to the top-level *keyvault* directory, where the *pom.xml* file is located.

1. Enter the following command:

   ```bash
   mvn spring-boot:run
   ```

   The command outputs `Completed initialization`, which indicates that the server is ready.

1. In a separate Bash window, enter the following command:

   ```bash
   curl http://localhost:8080/get
   ```

   The output will show `defaultValue`.

1. Kill the process that's running from `mvn spring-boot:run`. You can type Ctrl-C, or you can use the `jps` command to get the pid of the `Launcher` process and kill it.

## Create the app without Spring Initializr

This section shows how to include Azure Key Vault secrets to your existing Spring Boot project without using Spring Initializr.

To manually add the same the configuration that Spring Initializr generates, add the following configuration to your *pom.xml* file.

   ```xml
   <properties>
        <version.spring.cloud.azure>4.0.0</version.spring.cloud.azure>
   </properties>
   <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
        </dependency>
   </dependencies>
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

## Add Key Vault configuration to the app

This section shows you how to add Key Vault configuration to your locally running application by modifying the Spring Boot application `KeyvaultApplication`.

Just as Key Vault allows externalizing secrets from application code, Spring configuration allows externalizing configuration from code. The simplest form of Spring configuration is the *application.properties* file. In a Maven project, this file is located at *src/main/resources/application.properties*. Spring Initializr helpfully includes a zero length file at this location. Use the following steps to add the necessary configuration to this file.

1. Edit the *src/main/resources/application.properties* file so that it has the following contents, adjusting the values for your Azure subscription.

   ```txt
   spring.cloud.azure.keyvault.secret.property-source-enabled=true
   spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id=<your client ID>
   spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret=<your client key>
   spring.cloud.azure.keyvault.secret.property-sources[0].endpoint=https://contosokv.vault.azure.net/
   spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id=<your tenant ID>
   ```

   This table explains the properties shown above.

   | Parameter | Description |
   |---|---|
   | spring.cloud.azure.keyvault.secret.property-source-enabled | Whether enable the property source feature of spring-cloud-azure-starter-keyvault-secrets. Default value is false.
   | spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id | The `appId` from the return JSON from `az ad sp create-for-rbac`.|
   | spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret | The `password` from the return JSON from `az ad sp create-for-rbac`.|
   | spring.cloud.azure.keyvault.secret.property-sources[0].endpoint | The value output from the `az keyvault create` command above. |
   | spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id | The `tenant` from the return JSON from `az ad sp create-for-rbac`.|

   For the complete list of properties, see [Spring Cloud Azure Reference doc appendix](https://microsoft.github.io/spring-cloud-azure/current/reference/html/appendix.html#_configuration_properties).

1. Save the file and close it.

1. Open *src/main/java/com/contoso/keyvault/KeyvaultApplication.java* in an editor.

1. Add the following `import` statement.

   ```java
   import org.springframework.beans.factory.annotation.Value;
   ```

1. Add the following annotation to the `connectionString` instance variable.

   ```java
   @Value("${connectionString}")
   private String connectionString;
   ```

1. Open a Bash window and navigate to the top-level *keyvault* directory, where the *pom.xml* file is located.

1. Enter the following command:

   ```bash
   mvn clean package spring-boot:run
   ```

   The command outputs `initialization completed`, which indicates that the server is ready.

1. In a separate Bash window, enter the following command:

   ```bash
   curl http://localhost:8080/get
   ```

   The output will show `jdbc:sqlserver://SERVER.database.windows.net:1433;database=DATABASE` instead of `defaultValue`.

1. Kill the process that's running from `mvn spring-boot:run`. You can type Ctrl-C, or you can use the `jps` command to get the pid of the `Launcher` process and kill it.

## Deploy to Azure App Service

The following steps show you how to deploy the `KeyvaultApplication` to Azure App Service.

1. In the top-level *keyvault* directory, open the *pom.xml* file.
1. In the `<build><plugins>` section, add the `azure-webapp-maven-plugin` by inserting the following XML.

   ```xml
   <plugin>
     <groupId>com.microsoft.azure</groupId>
     <artifactId>azure-webapp-maven-plugin</artifactId>
     <version>2.2.2</version>
   </plugin>
   ```

   > [!NOTE]
   > Don't worry about the formatting. The `azure-webapp-maven-plugin` will reformat the entire POM during this process.

1. Save and close the *pom.xml* file
1. At a command line, use the following command to invoke the `config` goal of the newly added plugin.

   ```bash
   mvn azure-webapp:config
   ```

   The Maven plugin will ask you some questions and edit the *pom.xml* file based on the answers. Use the following values:

   * For **Subscription**, ensure you've selected the same subscription ID with the Key Vault you created.
   * For **Web App**, you can either select an existing Web App or select `<create>` to create a new one. If you select an existing Web App, it will jump directly to the last **confirm** step.
   * For **OS**, ensure **linux** is selected.
   * For **javaVersion**, ensure you select the Java version you chose in Spring Initializr. This tutorial uses version 11.
   * Accept the defaults for the remaining questions.
   * When asked to confirm, answer Y to continue or N to start answering the questions again. When the plugin completes running, you're ready to edit the POM.

1. Next, open the modified *pom.xml* in an editor. The contents of the file should be similar to the following XML. Replace the following placeholders with the specified values if you didn't already provide the value in the previous step.

   * `YOUR_SUBSCRIPTION_ID`: This placeholder shows the location of the ID provided previously.
   * `YOUR_RESOURCE_GROUP_NAME`: Replace this placeholder with the value that you specified when you created the Key Vault.
   * `YOUR_APP_NAME`: Replace this placeholder with a sensible value that's unique within your subscription.
   * `YOUR_REGION`: Replace this placeholder with the value that you specified when you created the Key Vault.
   * `APP_SETTINGS`: Copy the indicated `<appSettings>` element from the example and paste it into that location in your *pom.xml* file. This setting causes the server to listen on TCP port 80.

   ```xml
   <plugins>
     <plugin>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-maven-plugin</artifactId>
     </plugin>
     <plugin>
       <groupId>com.microsoft.azure</groupId>
       <artifactId>azure-webapp-maven-plugin</artifactId>
       <version>2.2.2</version>
       <configuration>
         <schemaVersion>V2</schemaVersion>
         <subscriptionId>YOUR_SUBSCRIPTION_ID</subscriptionId>
         <resourceGroup>YOUR_RESOURCE_GROUP_NAME</resourceGroup>
         <appName>YOUR_APP_NAME</appName>
         <pricingTier>P1v2</pricingTier>
         <region>YOUR_REGION</region>
         <runtime>
           <os>linux</os>
           <javaVersion>java 11</javaVersion>
           <webContainer>Java SE</webContainer>
         </runtime>
         <!-- start of APP_SETTINGS -->
         <appSettings>
           <property>
             <name>JAVA_OPTS</name>
             <value>-Dserver.port=80</value>
           </property>
         </appSettings>
         <!-- end of APP_SETTINGS -->
         <deployment>
           <resources>
             <resource>
               <directory>${project.basedir}/target</directory>
               <includes>
                 <include>*.jar</include>
               </includes>
             </resource>
           </resources>
         </deployment>
       </configuration>
     </plugin>
   </plugins>
   ```

1. Save and close the POM.
1. Use the following command to deploy the app to Azure App Service.

   ```bash
   mvn -DskipTests clean package azure-webapp:deploy
   ```

   This command may take several minutes, depending on many factors beyond your control. When you see output similar to the following example, you know your app has been successfully deployed.

   ```output
   [INFO] Deploying the zip package contosokeyvault-22b7c1a3-b41b-4082-a9f0-9339723fa36a11893059035499017844.zip...
   [INFO] Successfully deployed the artifact to https://contosokeyvault.azurewebsites.net
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   [INFO] Total time:  01:45 min
   [INFO] Finished at: 2020-08-16T22:47:48-04:00
   [INFO] ------------------------------------------------------------------------
   ```

1. Wait three to five minutes to allow the deployment to complete. Then you may access the deployment with a `curl` command similar to the one shown previously, but this time using the hostname shown in your `BUILD SUCCESS` output. The following example uses `contosokeyvault` as shown in the output above.

   ```bash
   curl https://contosokeyvault.azurewebsites.net/get
   ```

   The following output indicates success.

   ```output
   jdbc:sqlserver://SERVER.database.windows.net:1433;database=DATABASE;
   ```

You've now deployed your app to Azure App Service.

## Redeploy to Azure App Service and use managed identities for Azure resources

This section describes how to associate an identity with the Azure resource for the app. This association is required so that Azure can apply security and track access.

One of the foundational principles of cloud computing is to pay for only the resources you use. Such fine-grained resource tracking is only possible if every resource is associated with an identity. Azure App Service and Azure Key Vault are two of the many Azure services that take advantage of managed identities for Azure resources. For more information about this important technology, see [What are managed identities for Azure resources?](/azure/active-directory/managed-identities-azure-resources/overview)

> [!NOTE]
> "Managed identities for Azure resources" is the new name for the service formerly known as Managed Service Identity (MSI).

Use the following steps to create the managed identity for the Azure App Service app and then allow that identity to access the Key Vault.

1. Create a managed identity for the App Service app. Replace the `<your resource group name>` and `<your app name>` placeholders with the values of the `<resourceGroup>` and `<appName>` elements from your *pom.xml* file.

   ```azurecli
   az webapp identity assign --resource-group <your resource group name> --name <your app name>
   ```

   The output will be similar to the following example. Note down the value of `principalId` for the next step.

   ```json
   {
     "principalId": "<your principal ID>",
     "tenantId": "<your tenant ID>",
     "type": "SystemAssigned",
     "userAssignedIdentities": null
   }
   ```

1. Edit the *application.properties* so that it names the managed identity for Azure resources created in the preceding step.

   1. Remove the `spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret`.
   1. Update the `spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id` to have the value of the `principalId` from the preceding step. The completed file should now look like the following example.

   ```properties
   spring.cloud.azure.keyvault.secret.property-source-enabled=true
   spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id=<your principal ID>
   spring.cloud.azure.keyvault.secret.property-sources[0].credential.managed-identity-enabled=true
   spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id=<your tenant ID>
   spring.cloud.azure.keyvault.secret.property-sources[0].endpoint=https://contosokv.vault.azure.net/
   ```

1. Configure the Key Vault to allow `get` and `list` operations from the managed identity. The value of the `object-id` is the `principalId` from the preceding output.

   ```azurecli
   az keyvault set-policy \
       --name <your Key Vault name> \
       --object-id <your principal ID> \
       --secret-permissions get list
   ```

   The output will be a JSON object full of information about the Key Vault. It will have a `type` entry with value `Microsoft.KeyVault/vaults`

   This table explains the properties shown above.

   | Parameter | Description |
   |---|---|
   | name | The name of the Key Vault. |
   | object-id | The `principalId` from the preceding command. |
   | secret-permissions | The list of operations to allow from the named principal. |

1. Package and redeploy the application.

   ```bash
   mvn -DskipTests clean package azure-webapp:deploy
   ```

1. For good measure, wait a few more minutes to allow the deployment to settle down. Then you may contact the deployment with a `curl` command similar to the one shown previously, but this time using the hostname shown in your `BUILD SUCCESS` output. The following example uses `contosokeyvault` as shown in the `BUILD SUCCESS` output from the previous section.

   ```bash
   curl https://contosokeyvault.azurewebsites.net/get
   ```

   The following output indicates success.

   ```output
   jdbc:sqlserver://SERVER.database.windows.net:1433;database=DATABASE;
   ```

Instead of returning `defaultValue`, the app gets `connectionString` from the Key Vault.

## Deploy to Azure Spring Cloud

In this section, you'll deploy the app to Azure Spring Cloud.

Azure Spring Cloud is a fully managed platform for deploying and running your Spring Boot applications in Azure. For an overview of Azure Spring Cloud, see [What is Azure Spring Cloud?](/azure/spring-cloud/overview).

This section will use the Spring Boot app and Key Vault that you created previously with a new instance of Azure Spring Cloud.

The following steps will show how to create an Azure Spring Cloud resource and deploy the app to it. Make sure you've installed the Azure CLI extension for Azure Spring Cloud as shown in the [Prerequisites](#prerequisites).

1. Decide on a name for the service instance. To use Azure Spring Cloud within your Azure subscription, you must create an Azure resource of type Azure Spring Cloud. As with all other Azure resources, the service instance must stay within a resource group. Use the resource group you already created to hold the service instance, and choose a name for your Azure Spring Cloud instance. Create the service instance with the following command.

   ```azurecli
   az spring-cloud create --resource-group <your resource group name> --name <your Azure Spring Cloud instance name>
   ```

   This command takes several minutes to complete.

1. Create a Spring Cloud App within the service.

   ```azurecli
   az spring-cloud app create \
       --resource-group <your resource group name> \
       --service <your Azure Spring Cloud instance name> \
       --name <your app name> \
       --assign-identity \
       --is-public true \
       --runtime-version Java_11 \
   ```

   This table explains the options shown above.

   | Parameter | Description |
   |---|---|
   | resource-group | The name of the resource group where you created the existing service instance. |
   | service | The name of the existing service. |
   | name | The name of the app. |
   | assign-identity | Causes the service to create an identity for managed identities for Azure resources. |
   | is-public | Assign a public DNS domain name to the service. |
   | runtime-version | The Java runtime version. The value must match the value chosen in Spring Initializr above. |

   To understand the difference between *service* and *app*, see [App and deployment in Azure Spring Cloud](/azure/spring-cloud/concept-understand-app-and-deployment).

1. Use the following command to get the managed identity for the Azure resource and use it to configure the existing Key Vault to allow access from this App.

   ```azurecli
   SERVICE_IDENTITY=$(az spring-cloud app show --resource-group "contosorg" --name "contosoascsapp" --service "contososvc" | jq -r '.identity.principalId')
   az keyvault set-policy \
       --name <your Key Vault name> \
       --object-id <the value of the environment variable SERVICE_IDENTITY> \
       --secret-permissions set get list
   ```

1. Because the existing Spring Boot app already has an *application.properties* file with the necessary configuration, we can deploy this app directly to Spring Cloud using the following command. Run the command in the directory containing the POM.

   ```azurecli
   az spring-cloud app deploy \
       --resource-group <your resource group name> \
       --name <your Spring Cloud app name> \
       --jar-path target/keyvault-0.0.1-SNAPSHOT.jar \
       --service <your Azure Spring Cloud instance name>
   ```

   This command creates a *Deployment* within the app, within the service. For more details on the concepts of service instances, apps, and Deployments see [App and deployment in Azure Spring Cloud](/azure/spring-cloud/concept-understand-app-and-deployment).

   If the deployment isn't successful, configure the logs for troubleshooting as described in [Configure application logs](https://aka.ms/azure-spring-cloud-configure-logs). The logs will likely have useful information to diagnose and resolve the problem.

1. When the app has been successfully deployed, you can use `curl` to verify the Key Vault integration is working. Because you specified `--is-public`, the default URL for your service is `https://<your Azure Spring Cloud instance name>-<your app name>.azuremicroservices.io/`. The following command shows an example where the service instance name is `contososvc` and the app name is `contosoascsapp`. The URL appends the value of the `@GetMapping` annotation.

   ```bash
   curl https://contososvc-contosoascsapp.azuremicroservices.io/get
   ```

   The output will show `jdbc:sqlserver://SERVER.database.windows.net:1433;database=DATABASE`.

## Summary

In this tutorial, you created a new Java web application using the Spring Initializr. You created an Azure Key Vault to store sensitive information, and then configured your application to retrieve information from your Key Vault. After testing it locally, you deployed the app to Azure App Service and Azure Spring Cloud.

## Clean up resources

When you're finished with the Azure resources you created in this tutorial, you can delete them using the following command:

```azurecli
az group delete --name <your resource group name>
```

## Next steps

To learn more about `spring-cloud-azure-starter-keyvault-secrets`, refer to the [Spring Cloud Azure Reference documentation](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#secret-management)

> [!div class="nextstepaction"]
> [How to use the Spring Boot Starter for Azure Service Bus JMS](configure-spring-boot-starter-java-app-with-azure-service-bus.md)

[SI01]: media/spring-initializer/2.5.10/mvn-java8-keyvault-web.png