# Spring Cloud Azure Config

This project allows Spring Application to load properties from Azure Configuration Store.

## Samples

Please use this [sample](../../spring-cloud-azure-samples/azure-appconfiguration-sample/) as a reference for how to use this starter.

### Dependency Management

#### Maven Coordinates

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-starter-azure-appconfiguration-config</artifactId>
    <version>{starter-version}</version>
</dependency>

```

#### Gradle Coordinates

```gradle
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-starter-azure-appconfiguration-config', version: '{starter-version}'
}
```

## Supported properties

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores | List of configuration stores from which to load configuration properties | Yes | true
spring.cloud.azure.appconfiguration.enabled | Whether enable spring-cloud-azure-appconfiguration-config or not | No | true
spring.cloud.azure.appconfiguration.default-context | Default context path to load properties from | No | application
spring.cloud.azure.appconfiguration.name | Alternative to Spring application name, if not configured, fallback to default Spring application name | No | ${spring.application.name}
spring.cloud.azure.appconfiguration.profile-separator | Profile separator for the key name, e.g., /foo-app_dev/db.connection.key, must follow format `^[a-zA-Z0-9_@]+$` | No | `_`
spring.cloud.azure.appconfiguration.fail-fast | Whether throw RuntimeException or not when exception occurs | No |  true
spring.cloud.azure.appconfiguration.auto-refresh.enabled | Whether enable auto refresh feature or not | No | false
spring.cloud.azure.appconfiguration.auto-refresh.interval | Minimum interval of type [Duration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-conversion-duration) between two refresh checks | No | 30s
spring.cloud.azure.appconfiguration.managed-identity.client-id | Client id of the user assigned managed identity, only required when choosing to use user assigned managed identity on Azure | No | null

`spring.cloud.azure.appconfiguration.stores` is a List of stores, for each store should follow below format:

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores[0].endpoint | Endpoint of the configuration store, required when `connection-string` is empty. If `connection-string` is empty and application is deployed on Azure VM or App Service with managed identity enabled, will try to load `connection-string` from Azure Resource Management. | Conditional | null
spring.cloud.azure.appconfiguration.stores[0].prefix | The prefix of the key name in the configuration store, e.g., /my-prefix/application/key.name | No |  null
spring.cloud.azure.appconfiguration.stores[0].connection-string | Required when `name` is empty, otherwise, can be loaded automatically on Azure Virtual Machine or App Service | Conditional | null
spring.cloud.azure.appconfiguration.stores[0].label | Comma separated list of label values, by default will query empty labeled value. If you want to specify *empty*(null) label explicitly, use `%00`, e.g., spring.cloud.azure.appconfiguration.stores[0].label=%00,v0 | No |  null
spring.cloud.azure.appconfiguration.stores[0].watched-key | The single watched key(or by default *) used to indicate configuration change.  | No | *

## Advanced usage

### Load from multiple configuration stores

If the application needs to load configuration properties from multiple stores, following configuration sample describes how the bootstrap.properties(or .yaml) can be configured.

```properties
spring.cloud.azure.appconfiguration.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.appconfiguration.stores[0].prefix=[my-prefix]
spring.cloud.azure.appconfiguration.stores[0].label=[my-label]
spring.cloud.azure.appconfiguration.stores[1].connection-string=[second-store-connection-string]
```

If duplicate keys exists for multiple stores, the last configuration store has the highest priority.

### Load from multiple labels

If the application needs to load property values from multiple labels in the same configuration store, following configuration can be used:

```properties
spring.cloud.azure.appconfiguration.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.appconfiguration.stores[0].label=[my-label1], [my-label2]
```

Multiple labels can be separated with comma, if duplicate keys exists for multiple labels, the last label has highest priority.

### Auto Refresh configuration change

Auto Refresh feature allows the application to load the latest property value from configuration store automatically, without restarting the application.

By default, the auto refresh feature is disabled. It can be enabled with below configuration:

```properties
spring.cloud.azure.appconfiguration.auto-refresh.enabled=true

```properties

Change certain property key in the configuration store on Azure Portal, e.g., /application/config.message, log similar with below will be printed on the console.

```console
INFO 17496 --- [TaskScheduler-1] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [config.message]
```

The application now will be using the updated properties. By default, `@ConfigurationProperties` annotated beans will be automatically refreshed. Use `@RefreshScope` on beans which are required to be refreshed when properties are changed.
By default, all the keys in a configuration store will be watched. To prevent configuration changes are picked up in the middle of an update of multiple keys, you are recommended to use the watched-key property to watch a specific key that signals the completion of your update so all configuration changes can be refreshed together.

```properties
spring.cloud.azure.appconfiguration.stores[0].watched-key=[my-watched-key]
```

### Failfast

Failfast feature decides whether throw RuntimeException or not when exception happens. By default, failfast is enabled, it can be disabled with below configuration:

```properties
spring.cloud.azure.appconfiguration.fail-fast=false
```

### Use Managed Identity to access App Configuration

[Managed identity](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview) allows application to access [Azure Active Directory][azure_active_directory] protected resource on [Azure][azure].

In this library, [Azure Identity SDK][azure_identity_sdk] is used to access Azure App Configuration and optionally Azure Key Vault, for secrets. Only one method of authentication can be set at one time.

Follow the below steps to enable accessing App Configuration with managed identity:

1. [Enable managed identities](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview#how-can-i-use-managed-identities-for-azure-resources) for the [supported Azure services](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/services-support-managed-identities), for example, virtual machine or App Service, on which the application will be deployed.

1. Configure the [Azure RBAC][azure_rbac] of your Application store to grant access to the Azure service where your application is running. Select the App Configuration Data Reader. The App Configuration Data Owner role is not required but can be used if needed.

1. Choose a configuration option:
    1. Set the Environment variable; AZURE_CLIENT_ID.
    1. Create a TokenCredentialProvider and supply any valid TokenCredential and supply it via a Bean.
    1. Configure bootstrap.properties(or .yaml) in the Spring Boot application.

The configuration store name must be configured when `connection-string` is empty.

### Token Credential Provider

```java
public class MyCredentials implements AppConfigCredentialProvider, KeyVaultCredentialProvider {

    @Override
    public TokenCredential credentialForAppConfig() {
            return buildCredential();
    }

    @Override
    public TokenCredential credentialForKeyVault() {
            return buildCredential();
    }

    TokenCredential buildCredential() {
            return new DefaultAzureCredentialBuilder().build();
    }

}
```

### bootstrap.application

```application
spring.cloud.azure.appconfiguration.stores[0].endpoint=[config-store-endpoint]

#If Using option 3
spring.cloud.azure.appconfiguration.managed-identity.client-id=[client-id]

#If Using option 3
spring.cloud.azure.appconfiguration.managed-identity.client-id=[client-id]
```

<!-- LINKS -->
[azure]: https://azure.microsoft.com
[azure_active_directory]: https://azure.microsoft.com/services/active-directory/
[azure_identity_sdk]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[azure_rbac]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal
