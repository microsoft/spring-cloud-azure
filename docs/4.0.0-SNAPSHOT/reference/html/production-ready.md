# Production Ready

If you choose to add Spring Cloud Azure Actuator, include actuator dependencies.

```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-starter-actuator</artifactId>
</dependency>
```

## Enable Health Indicator

for Cosmos DB, add `management.health.azure-cosmos.enabled=true` to application.yaml.

```yml
management.health.azure-cosmos.enabled: true
```

// TODO add table for all servcies

> Warning: Call `http://{hostname}:{port}/actuator/health/cosmos` to get the Cosmos DB health info. **Please note**: it will calculate [RUs](https://docs.microsoft.com/azure/cosmos-db/request-units).

## Enable Sleuth

## Integrate with Azure Monitor


