# Spring Cloud Azure Config

This project allows Spring Application to load properties from Azure Configuration Store.

## Samples 

Please use this [sample](../../spring-cloud-azure-samples/azure-config-sample/) as a reference for how to use this starter. 

### Dependency Management

**Maven Coordinates** 
```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-starter-azure-config</artifactId>
</dependency>

```
**Gradle Coordinates** 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-starter-azure-config'
}
```

## Supported properties

Name | Description | Required | Default 
---|---|---|---
spring.cloud.azure.config.stores | List of configuration stores from which to load configuration properties | Yes | true
spring.cloud.azure.config.enabled | Whether enable spring-cloud-azure-config or not | No | true
spring.cloud.azure.config.default-context | Default context path to load properties from | No | application
spring.cloud.azure.config.name | Alternative to Spring application name, if not configured, fallback to default Spring application name | No | ${spring.application.name}
spring.cloud.azure.config.profile-separator | Profile separator for the key name, e.g., /foo-app_dev/db.connection.key, must follow format `^[a-zA-Z0-9_@]+$` | No | `_`
spring.cloud.azure.config.fail-fast | Whether throw RuntimeException or not when exception occurs | No |  true
spring.cloud.azure.config.msi-enabled | Whether load connection string from [Azure Resource Manager](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-overview) with [managed service identity](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview) token or not | No | false
spring.cloud.azure.config.watch.enabled | Whether enable watch feature or not | No | false
spring.cloud.azure.config.watch.delay | Polling interval in milli-seconds between each scheduled polling | No | 5000
spring.cloud.azure.config.msi.client-id | Client id of the managed identity | No | null
spring.cloud.azure.config.msi.object-id | Object id of the managed identity | No | null


`spring.cloud.azure.config.stores` is a List of stores, for each store should follow below format:

Name | Description | Required | Default 
---|---|---|---
spring.cloud.azure.config.stores[0].name | Required when `spring.cloud.azure.config.msi-enabled` is true, name of the configuration store | Conditional | null
spring.cloud.azure.config.stores[0].prefix | The prefix of the key name in the configuration store, e.g., /my-prefix/application/key.name | No |  null
spring.cloud.azure.config.stores[0].connection-string | Required when `spring.cloud.azure.config.msi-enabled` is false, otherwise, can be loaded automatically on Azure Virtual Machine or App Service | Conditional | null
spring.cloud.azure.config.stores[0].label | Comma separated list of label values | No |  null


## Advanced usage

### Load from multiple configuration stores
If the application needs to load configuration properties from multiple stores, following configuration sample describes how the bootstrap.properties(or .yaml) can be configured.
```
spring.cloud.azure.config.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.config.stores[0].prefix=[my-prefix]
spring.cloud.azure.config.stores[0].label=[my-label]
spring.cloud.azure.config.stores[1].connection-string=[second-store-connection-string]
```
If duplicate keys exists for multiple stores, the last configuration store has the highest priority.

### Load from multiple labels
If the application needs to load property values from multiple labels in the same configuration store, following configuration can be used:
```
spring.cloud.azure.config.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.config.stores[0].label=[my-label1], [my-label2]
```
Multiple labels can be separated with comma, if duplicate keys exists for multiple labels, the last label has highest priority.

### Watch configuration change
Watch feature allows the application to load the latest property value from configuration store automatically, without restarting the application.

By default, the watch feature is enabled. It can be disabled with below configuration:
```
spring.cloud.azure.config.watch.enabled=false
```

Change property key in the configuration store on Azure Portal, e.g., /application/config.message, log similar with below will be printed on the console.
```
INFO 17496 --- [TaskScheduler-1] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [config.message]
```
Refresh browser http://localhost:8080 should get the updated value.

### Failfast
Failfast feature decides whether throw RuntimeException or not when exception happens. By default, failfast is enabled, it can be disabled with below configuration:
```
spring.cloud.azure.config.fail-fast=false
```

### Use Azure Managed Identity to load the connection string
[Managed service identity](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview) allows application to access [Azure Active Directory](https://azure.microsoft.com/en-us/services/active-directory/) protected resource on [Azure](https://azure.microsoft.com/en-us/).

In this library, managed service identity is used to retrieve the connection string of the configuration store, the connection string is not required if running the Spring Boot application on Azure with managed service identity enabled.

Follow below steps to enable managed service identity feature:

1. [Enable managed identities service](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview#how-can-i-use-managed-identities-for-azure-resources) for virtual machine or App Service, on which the application will be deployed

2. Configure bootstrap.properties(or .yaml) in the Spring Boot application as following:
```
spring.cloud.azure.config.msi-enabled=true
spring.cloud.azure.config.stores[0].name=[msi-enabled-store-name]
```
The configuration store name must be configured when msi-enabled is true, the connection string for the configuration store will be loaded automatically.
